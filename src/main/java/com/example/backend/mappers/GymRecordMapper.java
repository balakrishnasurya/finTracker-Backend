package com.example.backend.mappers;


import com.example.backend.dtos.GymRecordDto;
import com.example.backend.entities.GymRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GymRecordMapper {

    GymRecord toGymRecord(GymRecordDto dto);

    GymRecordDto toGymRecordDto(GymRecord gymRecord);

    List<GymRecordDto> toGymReordDtos(List<GymRecord> gymRecords);


    @Mapping(target = "id" , ignore = true)
    void updateGymRecord(@MappingTarget GymRecord gymRecord, GymRecordDto gymRecordDto);
}
