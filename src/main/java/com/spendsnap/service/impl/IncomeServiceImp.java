package com.spendsnap.service.impl;

import com.spendsnap.entity.Income;
import com.spendsnap.repository.IncomeRepository;
import com.spendsnap.service.IncomeServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncomeServiceImp implements IncomeServices {

    @Autowired
    private IncomeRepository incomeRepository;

    @Override
    public boolean addIncome(Income income) {
        try{
            incomeRepository.save(income);
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
