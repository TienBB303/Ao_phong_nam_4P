package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Color;
import com.example.datn.entities.product_and_other.Color;
import com.example.datn.repositories.product_and_other.ColorRepository;
import com.example.datn.repositories.product_and_other.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColorService {
    @Autowired
    ColorRepository colorRepository;

    public List<Color> getAll(){
        return colorRepository.getAll();
    }

    public Page<Color> getAll(Pageable pageable){
        return colorRepository.getAll(pageable);
    }

    public Page<Color> searchPage(String name, Boolean status, Pageable pageable){
        return colorRepository.search(name,status, pageable);
    }

    public Color detail(Integer id){
        Color color = colorRepository.findByIdColor(id);
        return color;
    }

    public Color findByCode(String code){
        return colorRepository.findByCode(code);
    }

    public Color findByName(String name){
        return colorRepository.findByName(name);
    }

    public Color findById(Integer id){
        return colorRepository.findByIdColor(id);
    }
    public Color addColor(Color color){
        return colorRepository.save(color);
    }

    public Color addColor(String code, String name){
        Color color = new Color();
        color.setCode(code);
        color.setName(name);
        color.setStatus(true);
        return colorRepository.save(color);
    }

    public Color changeStatus(Integer id){
        if (id == null){
            System.out.println("khong co color id = " + id);
            return null;
        }
        Color color = colorRepository.findByIdColor(id);
        color.setStatus(!color.getStatus());
        return colorRepository.save(color);
    }

    public Color update(Integer id, String code, String colorName){
        if (id == null){
            System.out.println("khong co color id = " + id);
            return null;
        }
        Color color = colorRepository.findByIdColor(id);
        color.setCode(code);
        color.setName(colorName);
        return colorRepository.save(color);
    }

    public String findLastCodeColor(){
        return colorRepository.findMaxCodeColor();
    }
    public String taoMaTuDongColor(){
        String lastCode = findLastCodeColor();
        int nextCode = 1;

        if(lastCode != null && !lastCode.trim().isEmpty()){
            try{
                String numberPart = lastCode.substring(2); // lay so phia sau CL
                nextCode = Integer.parseInt(numberPart) + 1; // cong them 1
            }catch (NumberFormatException e){
//                hihi
            }
        }
        return String.format("CL%03d",nextCode);
    }
}
