package com.example.backend.services;


import com.example.backend.dtos.GymRecordDto;
import com.example.backend.entities.GymRecord;
import com.example.backend.exceptions.AppException;
import com.example.backend.mappers.GymRecordMapper;
import com.example.backend.repositories.GymRecordRepositories;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordsService {


    private final GymRecordRepositories gymRecordsRepository;
    private final GymRecordMapper gymRecordMapper;




    public List<GymRecordDto> allRecords() {
        return gymRecordMapper.toGymReordDtos(gymRecordsRepository.findAll());
    }

    public GymRecordDto createGymRecord(GymRecordDto gymRecordDto) {
        GymRecord gymRecord = gymRecordMapper.toGymRecord(gymRecordDto);

        GymRecord createdGymRecord = gymRecordsRepository.save(gymRecord);

        return gymRecordMapper.toGymRecordDto(createdGymRecord);
    }

    public @Nullable GymRecordDto deleteGymRecord(Long id) {
        GymRecord gymRecord = gymRecordsRepository.findById(id)
        .orElseThrow(() -> new AppException("GymRecord not found", HttpStatus.NOT_FOUND ));

        GymRecordDto gymRecordDto = gymRecordMapper.toGymRecordDto(gymRecord);

        gymRecordsRepository.deleteById(id);

        return gymRecordDto;

    }

    public @Nullable GymRecordDto updateGymRecord(Long id, GymRecordDto gymRecordDto) {
        GymRecord gymRecord = gymRecordsRepository.findById(id)
                .orElseThrow(()-> new AppException("Gym record Not Found",HttpStatus.NOT_FOUND) );

        gymRecordMapper.updateGymRecord(gymRecord,gymRecordDto);

        GymRecord savedGymRecord = gymRecordsRepository.save(gymRecord);

        return gymRecordMapper.toGymRecordDto(savedGymRecord);
    }
}
