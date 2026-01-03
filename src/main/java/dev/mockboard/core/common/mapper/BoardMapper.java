package dev.mockboard.core.common.mapper;

import dev.mockboard.storage.doc.BoardDoc;
import dev.mockboard.core.common.domain.dto.BoardDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardMapper {

    private final ModelMapper modelMapper;

    public BoardDto mapBoardDocToBoardDto(BoardDoc doc) {
        return modelMapper.map(doc, BoardDto.class);
    }
}
