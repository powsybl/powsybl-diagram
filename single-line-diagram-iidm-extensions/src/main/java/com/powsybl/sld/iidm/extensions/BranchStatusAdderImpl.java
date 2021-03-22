package com.powsybl.sld.iidm.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;

public class BranchStatusAdderImpl<C extends Connectable<C>> extends AbstractExtensionAdder<C, BranchStatus<C>>
        implements BranchStatusAdder<C> {

    private BranchStatus.Status status = BranchStatus.Status.IN_OPERATION;

    BranchStatusAdderImpl(C branch) {
        super(branch);
    }

    @Override
    protected BranchStatus createExtension(Connectable branch) {
        return new BranchStatusImpl(branch, status);
    }

    @Override
    public BranchStatusAdder withStatus(BranchStatus.Status branchStatus) {
        this.status = branchStatus;
        return this;
    }
}
