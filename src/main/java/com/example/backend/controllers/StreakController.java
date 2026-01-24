package com.example.backend.controllers;

import com.example.backend.dtos.StreakDto;
import com.example.backend.services.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/streak")
@Tag(name = "Streak", description = "App usage streak tracking")
public class StreakController {

    private final StreakService streakService;

    @GetMapping
    @Operation(
            summary = "Get app usage streak",
            description = "Returns the current streak information including current count, longest streak, and last update date",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Streak details retrieved successfully",
                            content = @Content(schema = @Schema(implementation = StreakDto.class)))
            }
    )
    public ResponseEntity<StreakDto> getStreak() {
        return ResponseEntity.ok(streakService.getStreak());
    }

    @PostMapping
    @Operation(
            summary = "Update app usage streak",
            description = "Marks the app as used today. Updates the streak count if used consecutively, or resets if there's a gap",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Streak updated successfully",
                            content = @Content(schema = @Schema(implementation = StreakDto.class)))
            }
    )
    public ResponseEntity<StreakDto> updateStreak() {
        return ResponseEntity.ok(streakService.updateStreak());
    }
}
