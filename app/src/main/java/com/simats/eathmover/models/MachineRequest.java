package com.simats.eathmover.models;

public class MachineRequest {

    private int machine_id;

    public MachineRequest(int machine_id) {
        this.machine_id = machine_id;
    }

    public int getMachine_id() {
        return machine_id;
    }

    public void setMachine_id(int machine_id) {
        this.machine_id = machine_id;
    }
}
