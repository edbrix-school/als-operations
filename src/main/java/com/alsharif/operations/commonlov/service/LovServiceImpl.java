package com.alsharif.operations.commonlov.service;
import com.alsharif.operations.commonlov.dto.LovItem;
import com.alsharif.operations.commonlov.dto.LovResponse;
import com.alsharif.operations.commonlov.repository.LovRepository;
import com.alsharif.operations.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LovServiceImpl implements LovService {

    @Autowired
    LovRepository lovRepository;

    @Override
    public LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue, Long groupPoid, Long companyPoid, String userId) {
        log.info("Fetching LOV list for lovName={} docKeyPoid={} filterValue={} groupPoid={} companyPoid={} userId={}", 
                lovName, docKeyPoid, filterValue, groupPoid, companyPoid, userId);
        LovResponse response = lovRepository.getLovList(lovName, docKeyPoid, filterValue, groupPoid, companyPoid, userId);
        log.info("Fetched LOV list for lovName={} itemCount={}", lovName,
                response != null && response.getItems() != null ? response.getItems().size() : 0);
        return response;
    }

    @Override
    public LovItem getLovItem(Long poid, String lovName, Long groupPoid, Long companyPoid, String userId) {
        log.info("poid : {}, lovName : {}, groupPoid : {}, companyPoid : {}, userId : {}", 
                poid, lovName, groupPoid, companyPoid, userId);

        LovItem dto = new LovItem();
        LovResponse listValue = this.getLovList(lovName, poid, "", groupPoid, companyPoid, userId);

        if (listValue != null) {

            List<LovItem> lovGetListDtos = listValue.getItems();

            if (lovGetListDtos != null) {

                dto = lovGetListDtos.stream().filter(x -> x.getPoid().equals(poid)).findAny().orElse(new LovItem(poid, null, null, null, null, null));

            }
        }
        return dto;
    }

}
