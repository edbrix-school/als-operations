package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.GlobalTermsCustomChanges;
import com.asg.operations.salesquotationprojects.key.GlobalTermsCustomChangesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GlobalTermsCustomChangesRepository extends JpaRepository<GlobalTermsCustomChanges, GlobalTermsCustomChangesId> {
    List<GlobalTermsCustomChanges> findByIdDocIdAndIdDocKeyPoidAndIdRefTermsPoid(String docId, Long docKeyPoid, Long refTermsPoid);
    
    List<GlobalTermsCustomChanges> findByIdDocIdAndIdDocKeyPoid(String docId, Long docKeyPoid);
    
    @Query("SELECT COALESCE(MAX(t.id.detRowId), 0) FROM GlobalTermsCustomChanges t WHERE t.id.docId = :docId AND t.id.docKeyPoid = :docKeyPoid AND t.id.refTermsPoid = :refTermsPoid")
    Long findMaxDetRowIdByDocIdAndDocKeyPoidAndRefTermsPoid(@Param("docId") String docId, @Param("docKeyPoid") Long docKeyPoid, @Param("refTermsPoid") Long refTermsPoid);
}
