package com.asg.operations.commonlov.service;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.dto.LovResponse;
import com.asg.operations.commonlov.repository.LovRepository;
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
    public LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue, Long groupPoid, Long companyPoid, Long userPoid) {
        log.info("Fetching LOV list for lovName={} docKeyPoid={} filterValue={} groupPoid={} companyPoid={} userId={}", 
                lovName, docKeyPoid, filterValue, groupPoid, companyPoid, userPoid);
        LovResponse response = lovRepository.getLovList(lovName, docKeyPoid, filterValue, groupPoid, companyPoid, userPoid);
        log.info("Fetched LOV list for lovName={} itemCount={}", lovName,
                response != null && response.getItems() != null ? response.getItems().size() : 0);
        return response;
    }

    @Override
    public LovItem getLovItem(Long poid, String lovName, Long groupPoid, Long companyPoid, Long userPoid) {
        log.info("poid : {}, lovName : {}, groupPoid : {}, companyPoid : {}, userId : {}", 
                poid, lovName, groupPoid, companyPoid, userPoid);

        LovItem dto = new LovItem();
        LovResponse listValue = this.getLovList(lovName, poid, "", groupPoid, companyPoid, userPoid);

        if (listValue != null) {

            List<LovItem> lovGetListDtos = listValue.getItems();

            if (lovGetListDtos != null) {

                dto = lovGetListDtos.stream().filter(x -> x.getPoid().equals(poid)).findAny().orElse(new LovItem(poid, null, null, null, null, null));

            }
        }
        return dto;
    }

}
