package io.github.askmeagain.bookmarkkeeper.extensions;

import com.intellij.openapi.project.Project;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.context.WorkingContextProvider;
import io.github.askmeagain.bookmarkkeeper.BookmarkKeeperManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class BookmarkKeeperContextProviderBefore extends WorkingContextProvider {
  @NotNull
  @Override
  public String getId() {
    return "bookmarkKeeperBefore";
  }

  @NotNull
  @Override
  public String getDescription() {
    return TaskBundle.message("bookmarks");
  }

  @Override
  public void saveContext(@NotNull Project project, @NotNull Element toElement) {
    BookmarkKeeperManager.getInstance().saveBookmarks(project);
  }

  @Override
  public void loadContext(@NotNull Project project, @NotNull Element fromElement) {
  }

  @Override
  public void clearContext(@NotNull Project project) {
  }
}