package com.example.backend.mappers;

import com.example.backend.dtos.StreakDto;
import com.example.backend.dtos.StreakUpdateResponseDto;
import com.example.backend.entities.Streak;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StreakMapper {

    StreakDto toStreakDto(Streak streak);

    List<StreakDto> toStreakDtos(List<Streak> streaks);

    StreakUpdateResponseDto toStreakUpdateResponseDto(Streak streak);
}
