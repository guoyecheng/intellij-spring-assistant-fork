package in.oneton.idea.spring.assistant.plugin.suggestion.component;

import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.project.Project;
import in.oneton.idea.spring.assistant.plugin.suggestion.service.SuggestionService;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BuildReIndexListener implements BuildManagerListener {
    @Override
    public void buildFinished(@NotNull final Project project, @NotNull final UUID sessionId, final boolean isAutomake) {
        SuggestionService service = project.getService(SuggestionService.class);
        service.reIndex(project);
    }
}
