package com.rcraja.swapndie.game;

public enum ArenaState {
    BUILDING,   // creator is placing the trap, spawn/win points not both set yet
    ARMED,      // both points set, countdown timer running
    ACTIVE,     // opponent has been swapped in, round is live
    FINISHED    // round ended (win or loss)
}

