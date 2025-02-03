package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkGroup;
import com.intellij.ide.bookmark.BookmarksManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import kotlin.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public final class BookmarkKeeperManager {
  private static final SimpleLogger log = new SimpleLogger("BookmarkKeeper");
  private List<Container> bookmarks;
  private List<String> groupNames;

  public static BookmarkKeeperManager getInstance() {
    log.debug("Getting instance");
    return ApplicationManager.getApplication().getService(BookmarkKeeperManager.class);
  }

  public void saveBookmarks(Project project) {
    log.info("Save bookmarks started, project: " + project.getName());
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null) return;

      // Save group names and their indexes
      List<Pair<String, Integer>> groupNamesWithIndexes = bookmarksManager.getGroups()
          .stream()
          .map(group -> new Pair<>(group.getName(), bookmarksManager.getGroups().indexOf(group)))
          .collect(Collectors.toList());

      groupNames = groupNamesWithIndexes.stream()
          .map(Pair::getFirst)
          .collect(Collectors.toList());
      log.info("Saved " + groupNames.size() + " group names with indexes");
      // Save bookmarks with their groups and types
      bookmarks = bookmarksManager.getBookmarks()
          .stream()
          .map(bookmark -> {
            BookmarkGroup group = bookmarksManager.getGroups(bookmark)
                .stream()
                .findFirst()
                .orElse(null);
            return new Container(
                bookmarksManager.getType(bookmark),
                bookmark,
                group != null ? group.getName() : null
            );
          })
          .collect(Collectors.toList());
      log.info("Saved bookmarks count: " + bookmarks.size());
    } catch (Exception e) {
      log.error("Failed to save bookmarks", e);
    }
  }

  public void loadBookmarks(Project project) {
    log.info("Load bookmarks started, project: " + project.getName());
    try {
      final BookmarksManager bookmarksManager = BookmarksManager.getInstance(project);
      if (bookmarksManager == null || bookmarks == null) {
        log.warn("Bookmarks manager is null or no bookmarks to load");
        return;
      }

      log.info("Removing existing bookmarks and groups");
      bookmarksManager.remove();

      // Create a temporary default group
      log.info("Creating temporary default group");
      BookmarkGroup tempDefaultGroup = bookmarksManager.addGroup("__temp_default__", true);

      // Add all bookmarks to temp default group
      log.info("Adding " + bookmarks.size() + " bookmarks to temporary default group");
      for (Container container : bookmarks) {
        bookmarksManager.add(container.bookmark, container.type);
      }

      // Recreate original groups
      log.info("Recreating " + groupNames.size() + " original groups");
      Map<String, BookmarkGroup> groupMap = new HashMap<>();
      BookmarkGroup originalDefaultGroup = null;
      for (String groupName : groupNames) {
        BookmarkGroup newGroup = bookmarksManager.addGroup(groupName, false);
        if (newGroup != null) {
          groupMap.put(groupName, newGroup);
          log.debug("Recreated group: " + groupName);

          // Find the original default group (typically project name group)
          if (groupName.equals(project.getName())) {
            originalDefaultGroup = newGroup;
            log.info("Identified original default group: " + groupName);
          }
        } else {
          log.warn("Failed to recreate group: " + groupName);
        }
      }

      // Get saved bookmarks
      final List<Bookmark> savedBookmarks = bookmarksManager.getBookmarks();
      log.info("Total saved bookmarks: " + savedBookmarks.size());

      // Move bookmarks to their original groups
      int movedBookmarks = 0;
      int failedToMoveBookmarks = 0;
      for (Container container : bookmarks) {
        Optional<Bookmark> matchingBookmark = savedBookmarks.stream()
            .filter(b -> b.getAttributes().equals(container.bookmark.getAttributes()))
            .findFirst();

        if (matchingBookmark.isPresent()) {
          Bookmark bookmark = matchingBookmark.get();
          if (container.groupName != null && !container.groupName.isEmpty()) {
            BookmarkGroup targetGroup = groupMap.get(container.groupName);
            if (targetGroup != null) {
              targetGroup.add(bookmark, container.type, null);
              movedBookmarks++;
              log.debug("Moved bookmark to group: " + container.groupName);
            } else {
              failedToMoveBookmarks++;
              log.warn("Failed to move bookmark to group: " + container.groupName);
            }
          }
        } else {
          failedToMoveBookmarks++;
          log.warn("No matching bookmark found for container");
        }
      }

      // Set the original default group
      if (originalDefaultGroup != null) {
        log.info("Setting original default group: " + originalDefaultGroup.getName());
        originalDefaultGroup.setDefault(true);
      } else {
        log.warn("No original default group found to set");
      }

      // Remove temporary default group
      log.info("Removing temporary default group");
      tempDefaultGroup.remove();

      log.info("Bookmark loading summary:");
      log.info("Total groups: " + groupNames.size());
      log.info("Total bookmarks: " + bookmarks.size());
      log.info("Successfully moved bookmarks: " + movedBookmarks);
      log.info("Failed to move bookmarks: " + failedToMoveBookmarks);
    } catch (Exception e) {
      log.error("Failed to load bookmarks", e);
    }
  }
}