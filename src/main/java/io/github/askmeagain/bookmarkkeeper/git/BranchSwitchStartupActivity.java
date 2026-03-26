package io.github.askmeagain.bookmarkkeeper.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vcs.BranchChangeListener;
import io.github.askmeagain.bookmarkkeeper.BookmarkKeeperManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BranchSwitchStartupActivity implements ProjectActivity {

  @Override
  public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
    project.getMessageBus().connect(project).subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, new BranchChangeListener() {
      @Override
      public void branchWillChange(@NotNull String branchName) {
        BookmarkKeeperManager.getInstance(project).OnContextClosed();
      }
      
      @Override
      public void branchHasChanged(@NotNull String branchName) {
        BookmarkKeeperManager.getInstance(project).OnContextOpened();
      }
    });

    return null;
  }
}
