package com.spendsnap.Services.Implementation;

import com.spendsnap.Entities.Income;
import com.spendsnap.Repositories.IncomeRepository;
import com.spendsnap.Services.IncomeServices;
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
