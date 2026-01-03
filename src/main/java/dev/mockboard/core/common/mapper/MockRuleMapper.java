package dev.mockboard.core.common.mapper;

import dev.mockboard.storage.doc.MockRuleDoc;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockRuleMapper {

    private final ModelMapper modelMapper;

    public MockRuleDoc mapMockRuleDtoToMockRuleDoc(MockRuleDto mockRuleDto) {
        return modelMapper.map(mockRuleDto, MockRuleDoc.class);
    }

    public MockRuleDto mapMockRuleDocToMockRuleDto(MockRuleDoc mockRuleDoc) {
        return modelMapper.map(mockRuleDoc, MockRuleDto.class);
    }
}
