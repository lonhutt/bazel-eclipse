package com.salesforce.bazel.eclipse.projectimport.flow;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;

import com.salesforce.bazel.eclipse.runtime.api.ResourceHelper;
import com.salesforce.bazel.sdk.command.BazelCommandManager;
import com.salesforce.bazel.sdk.logging.LogHelper;
import com.salesforce.bazel.sdk.project.BazelProjectManager;

public class CleanProjectsFlow extends AbstractImportFlowStep {

    private static final LogHelper LOG = LogHelper.log(CleanProjectsFlow.class);

    public CleanProjectsFlow(BazelCommandManager commandManager, BazelProjectManager projectManager,
            ResourceHelper resourceHelper) {
        super(commandManager, projectManager, resourceHelper);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run(ImportContext ctx, SubMonitor progressMonitor) throws Exception {

        IProject[] proj = getResourceHelper().getProjectsForBazelWorkspace(getBazelWorkspace());
        Set<String> selectedPackages = ctx.getSelectedBazelPackages().stream()
                .map(sp -> sp.getBazelPackageNameLastSegment()).collect(Collectors.toSet());

        // make sure not to remove the 'special' root project (ie. any project without structure). TODO: validate this assumption
        selectedPackages.addAll(this.getProjectManager().getAllProjects().stream()
                .filter(bp -> bp.projectStructure == null).map(p -> p.name).collect(Collectors.toList()));

        Stream.of(proj).filter((p) -> !selectedPackages.contains(p.getName())).forEach(p -> {
            try {
                p.delete(true, progressMonitor);
            } catch (CoreException e) {
                LOG.error(e.getLocalizedMessage(), p);
            }
        });

        LOG.debug("cleanup removed modules", this);

    }

    @Override
    public String getProgressText() {
        return "Cleaning the language server of unloaded modules.";
    }

}
