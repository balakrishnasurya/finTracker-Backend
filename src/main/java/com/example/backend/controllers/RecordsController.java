package com.example.backend.controllers;


import com.example.backend.dtos.GymRecordDto;
import com.example.backend.services.RecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/gym/records")
public class RecordsController {

    private final RecordsService recordsService;

    @GetMapping
    public ResponseEntity<List<GymRecordDto>> allRecords() {
        return ResponseEntity.ok(recordsService.allRecords());
    }

    @PostMapping
    public ResponseEntity<GymRecordDto> createGymRecord(
            @RequestBody GymRecordDto gymRecordDto) {

        GymRecordDto created = recordsService.createGymRecord(gymRecordDto);

        return ResponseEntity
                .created(URI.create("/gym/records/" + created.getId()))
                .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GymRecordDto> deleteGymRecord(@PathVariable Long id) {
        return ResponseEntity.ok(recordsService.deleteGymRecord(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GymRecordDto> updateGymRecord(
            @PathVariable Long id,
            @RequestBody GymRecordDto gymRecordDto) {

        return ResponseEntity.ok(
                recordsService.updateGymRecord(id, gymRecordDto)
        );
    }
}
