package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;

public class BranchStatusImpl<C extends Connectable<C>> extends AbstractExtension<C> implements BranchStatus<C> {

    private Status status;

    public BranchStatusImpl(C branch, Status branchStatus) {
        super(branch);
        this.status = branchStatus;
    }

    public Status getStatus() {
        return status;
    }

    public BranchStatus setStatus(Status branchStatus) {
        this.status = branchStatus;
        return this;
    }

}
