package com.comment.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class CustomWriteListener implements ItemWriteListener {

    private final String COUNT_KEY = "countKey";
    private final StringRedisTemplate redisTemplate;

    @Override
    public void beforeWrite(Chunk items) {
        ItemWriteListener.super.beforeWrite(items);
    }

    @Override
    public void afterWrite(Chunk items) {

        Long end = redisTemplate.opsForZSet().size(COUNT_KEY);
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(COUNT_KEY, 0, end);

        try {
            log.info("파일이 생성됨");
            BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/result.txt"));
            for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
                String schoolName = typedTuple.getValue();
                Double score = typedTuple.getScore();
                writer.write(schoolName + " " + score.intValue());
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.delete(COUNT_KEY);
    }

    @Override
    public void onWriteError(Exception exception, Chunk items) {
        ItemWriteListener.super.onWriteError(exception, items);
    }
}
