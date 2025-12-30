package com.numlock.pika.controller.user;

import com.numlock.pika.dto.user.AdditionalUserInfoDto;
import com.numlock.pika.repository.UserRepository;
import com.numlock.pika.service.user.LoginService;
import com.numlock.pika.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final LoginService loginService;

    //회원 추가정보 입력 처리(Update)
    @GetMapping("/user/add-info")
    public String addInfoForm(Principal principal, Model model) {
        if(principal != null){
            userRepository.findById(principal.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
                AdditionalUserInfoDto dto = new AdditionalUserInfoDto();
                dto.setPhone(user.getPhone()); //휴대폰 정보 불러오기
                if(user.getBirth() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
                    dto.setBirth(sdf.format(user.getBirth()));
                }//생년월일 정보 불러오기
                dto.setAddress(user.getAddress()); //주소정보 불러오기
                model.addAttribute("userAddInfo", dto);
            });
        }else {
            model.addAttribute("userAddInfo", new AdditionalUserInfoDto());
        }
        return "user/addInfoForm";
    }

    @PostMapping("/user/add-info")
    public String addInfo(@Valid @ModelAttribute("userAddInfo") AdditionalUserInfoDto dto,
                          BindingResult result, Principal principal, Model model,
                          @RequestParam(value="profileImageFile", required = false)
                          MultipartFile profileImageFile, RedirectAttributes redirectAttributes) {
        if(principal == null) {
            //미로그인 오류 처리
            return "redirect:/";
        }
        //상세주소 조건부 유효성 검사
        /*if (dto.getAddress() != null && !dto.getAddress().isBlank()
                && (dto.getDetailAddress() == null || dto.getDetailAddress().isBlank())){
            result.rejectValue("detailAddress", "blankDetailAddress", "상세주소를 입력하세요.");
        }*/

        //유효성 검사
        if(result.hasErrors()){
            log.error("--Additional User Info Validation Failed-- {}", result);
            userRepository.findById(principal.getName()).ifPresent(user -> model.addAttribute("user", user));
            model.addAttribute("userAddInfo", dto);
            return "user/addInfoForm";
        }
        try {
            userService.updateAddlInfo(principal.getName(), dto, profileImageFile);
            redirectAttributes.addFlashAttribute("successMessage", "수정완료되었습니다.");
            return "redirect:/";
        }catch (IllegalArgumentException e){
            log.error("추가 정보 업데이트 중 오류 발생: {}", e.getMessage());
            if("전화번호 형식이 올바르지 않습니다.".equals(e.getMessage())){
                result.rejectValue("phone", "invalid.phone", e.getMessage());
            }else if("생년월일 형식이 올바르지 않습니다.".equals(e.getMessage())){
                result.rejectValue("birth", "invalid.birth", e.getMessage());
            }else {
                result.reject("globalError", e.getMessage());
            }

            model.addAttribute("errorMessage", "입력 내용을 다시 확인해주세요.");
            userRepository.findById(principal.getName()).ifPresent(user -> model.addAttribute("user", user));
            model.addAttribute("userAddInfo", dto);
            return "user/addInfoForm";
        }catch (Exception e){
            log.error("추가 정보 업데이트 중 예상치 못한 오류 발생: {}", e.getMessage());
            result.reject("globalError", "알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
            model.addAttribute("errorMessage", "알 수 없는 오류가 발생했습니다. 다시 시도해주세요.");
            userRepository.findById(principal.getName()).ifPresent(user -> model.addAttribute("user", user));
            model.addAttribute("userAddInfo", dto);
            return "user/addInfoForm";
        }
    }

    //회원 탈퇴 처리(Delete)
    @GetMapping("/user/delete")
    public String deleteForm() {
        return "/user/deleteForm";
    }

    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam String rawPassword, Principal principal, HttpServletRequest request,
                             Model model, RedirectAttributes redirectAttributes) {
        if(principal == null) {
            return "redirect:/user/login"; //로그인 하지 않은 사용자 접근시
        }
        try {
            loginService.deleteUser(principal.getName(), rawPassword);
            request.logout();
            redirectAttributes.addFlashAttribute("successMessage", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "/user/deleteForm";
        }
    }
}
