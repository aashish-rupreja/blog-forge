package com.blogforge.dto.reaction;

import com.blogforge.entity.ReactionType;

public record AddReactionRequest(
        ReactionType reactionType
) {
}
