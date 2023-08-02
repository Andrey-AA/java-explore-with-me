package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationDto;
import ru.practicum.service.CompilationService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
@RequiredArgsConstructor
public class CompilationController {

    private final CompilationService compilationService;

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.debug("Админ добавляет подборку событий newCompilationDto = {} ", newCompilationDto);
        return compilationService.createCompilation(newCompilationDto);
    }

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable int compId, @RequestBody @Valid UpdateCompilationDto updateCompilationDto) {
        log.debug("Админ обновляет подборку событий c compId = {} updateCompilationDto = {}", compId, updateCompilationDto);
        return compilationService.updateCompilation(compId, updateCompilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int compId) {
        log.debug("Админ Удаляет подборка с идентификатором: {}", compId);
        compilationService.delete(compId);
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> findAll(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                        @PositiveOrZero @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                        @Positive @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        List<CompilationDto> compilationDtoList = compilationService.findAll(pinned, from, size);
        log.debug("Количество подборок в текущий момент: {}", compilationDtoList.size());
        return compilationDtoList;
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto findAll(@PathVariable int compId) {
        log.debug("Ищется подборка по compId = {}", compId);
        return compilationService.getCompilationById(compId);
    }
}
