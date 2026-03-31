package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.ShipCommodityMaster;
import com.asg.operations.salesquotationprojects.key.ShipCommodityMasterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ShipCommodityMasterRepository extends JpaRepository<ShipCommodityMaster, ShipCommodityMasterId> {
    boolean existsById(@NonNull ShipCommodityMasterId id);
}
