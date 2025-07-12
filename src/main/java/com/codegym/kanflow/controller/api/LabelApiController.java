package com.codegym.kanflow.controller.api;

import com.codegym.kanflow.dto.LabelDto;
import com.codegym.kanflow.model.Label;
import com.codegym.kanflow.service.ILabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/labels")
public class LabelApiController {
    @Autowired
    private ILabelService labelService;

    @GetMapping
    public ResponseEntity<List<LabelDto>> getAllLabels() {
        List<Label> labels = labelService.findAll();
        List<LabelDto> dtos = labels.stream().map(label -> {
            LabelDto dto = new LabelDto();
            dto.setId(label.getId());
            dto.setName(label.getName());
            dto.setColor(label.getColor());
            return dto;
        }).collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
}
