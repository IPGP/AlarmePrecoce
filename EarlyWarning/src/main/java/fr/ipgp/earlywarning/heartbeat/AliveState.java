package fr.ipgp.earlywarning.heartbeat;

public enum AliveState {
    Alive,
    CantConnect,
    CantWrite,
    NoResponse,
    Error
}
