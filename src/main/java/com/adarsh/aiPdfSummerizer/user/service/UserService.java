package com.adarsh.aiPdfSummerizer.user.service;

import com.adarsh.aiPdfSummerizer.common.exception.ResourceNotFoundException;
import com.adarsh.aiPdfSummerizer.user.dto.UserResponse;
import com.adarsh.aiPdfSummerizer.user.entity.User;
import com.adarsh.aiPdfSummerizer.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User","id",id));
        return UserResponse.from(user);
    }

    public UserResponse getByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User","email",email));
        return UserResponse.from(user);
    }

    public List<UserResponse> getAll(){
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public void deactivate(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User","id",id));
        user.setEnabled(false);
        userRepository.save(user);
    }
}
