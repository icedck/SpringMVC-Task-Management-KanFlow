package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Label;
import com.codegym.kanflow.repository.LabelRepository;
import com.codegym.kanflow.service.ILabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelService implements ILabelService {
    @Autowired
    private LabelRepository labelRepository;

    @Override
    public List<Label> findAll() {
        return labelRepository.findAll();
    }
}