package io.github.askmeagain.bookmarkkeeper.state;

import io.github.askmeagain.bookmarkkeeper.dtos.BookmarkContainer;

import java.util.ArrayList;
import java.util.List;

public final class StateData {
  public List<BookmarkContainer> allBookmarks = new ArrayList<>();
  public List<BookmarkContainer> notExistingBookmarks = new ArrayList<>();
}
