package com.nexusbpm.dataselector.controller;

public interface Controller {
    boolean isActive();
    void refresh();
    void refreshChildren();
    void refreshSourceConnections();
    void refreshTargetConnections();
    void refreshVisuals();
}
