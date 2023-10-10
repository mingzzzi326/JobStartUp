package com.pickmeup.jobstartup.member.service;

import com.pickmeup.jobstartup.member.dto.JoinCommonDTO;
import com.pickmeup.jobstartup.member.entity.Member;
import com.pickmeup.jobstartup.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    //개인회원 아이디 중복 여부 검사
    @Override
    public boolean isDuplicateMemberId(String memberId) {
        int count = memberRepository.countByMemberId(memberId);
        return count > 0;
    }

    //회원가입
    @Override
    public void join(JoinCommonDTO joinCommonDTO) {
        if (!isValidDate(joinCommonDTO.getMember_birth())) {
            throw new IllegalArgumentException("YYYYMMDD 입력 형식을 확인해 주세요");
        }

        Member member = convertDtoToEntity(joinCommonDTO);

        String encryptedPassword = passwordEncoder.encode(joinCommonDTO.getMember_pw());
        member.setMember_pw(encryptedPassword);

        String menuId = getMemberMenuId(joinCommonDTO.getMember_type().getCode());
        member.setMenu_id(menuId);

        memberRepository.save(member);
    }

    //회원 구분에 따른 메뉴ID 가져오기
    @Override
    public String getMemberMenuId(String memberType) {
        return memberRepository.selectMenuId(memberType);
    }

    //joinCommonDTO를 Member엔티티로 바꾸기
    private Member convertDtoToEntity(JoinCommonDTO joinCommonDTO) {
        Member member = modelMapper.map(joinCommonDTO, Member.class);
        return member;
    }

    //생년월일 유효성 검사
    public boolean isValidDate(String birthDate) {
        if (birthDate.length() != 8) {
            return false;
        }

        try {
            Integer.parseInt(birthDate);
        } catch (NumberFormatException e) {
            return false;
        }

        int year = Integer.parseInt(birthDate.substring(0, 4));
        int month = Integer.parseInt(birthDate.substring(4, 6));
        int day = Integer.parseInt(birthDate.substring(6, 8));

        if (month < 1 || month > 12 || day < 1 || day > 31) {
            return false;
        }

        if ((month == 4 || month == 6 || month == 9 || month == 11) && day == 31) {
            return false;
        }

        if (month == 2) {
            if (day > 29) {
                return false;
            }
            if (day == 29) {
                if (!(year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))) {
                    return false;
                }
            }
        }

        return true;
    }
}