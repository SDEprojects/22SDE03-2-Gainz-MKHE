package com.games.gobigorgohome.app;

import com.apps.util.Prompter;
import com.games.gobigorgohome.characters.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class GameTest {
    List requiredItems = new ArrayList();
    List noRequiredItems = new ArrayList();

    @Before
    public void createRequiredItemList() {
        requiredItems.add("wrench");
    }

    @Before
    public void createNoRequiredItemList() {
        noRequiredItems.add("none");
    }

    @Test
    public void isItemRequiredShouldReturnFalseIfNoItemsAreRequired() {
        assertFalse(Game.isItemRequired(noRequiredItems));
    }

    @Test
    public void isItemRequiredShouldReturnTrueIfItemsAreRequired() {
        assertTrue(Game.isItemRequired(requiredItems));
    }

}