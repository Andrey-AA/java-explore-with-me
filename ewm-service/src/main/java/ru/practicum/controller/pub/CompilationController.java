package ru.practicum.controller.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationController {

    private final CompilationService compilationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> findAll(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                        @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                        @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<CompilationDto> compilationDtoList = compilationService.findAll(pinned, from, size);
        log.debug("Количество подборок в текущий момент: {}", compilationDtoList.size());
        return compilationDtoList;
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto findAll(@PathVariable int compId) {
        log.debug("Ищется подборка по compId = {}", compId);
        return compilationService.getCompilationById(compId);
    }
}
