package com.comment.batch.config;

import com.comment.batch.entity.School;
import com.comment.batch.listener.CustomJobListener;
import com.comment.batch.listener.CustomWriteListener;
import com.comment.batch.repository.SchoolRepository;
import com.comment.batch.vo.Comment;
import com.comment.batch.vo.SchoolCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommentBatchConfig {

    private final int CHUNK_SIZE = 500;
    private final SchoolRepository schoolRepository;
    private final StringRedisTemplate redisTemplate;
    private final String COUNT_KEY = "countKey";

    private Map<String,Integer> resultScore= new HashMap<>();
    @Bean
    public Job commentJob(JobRepository jobRepository, Step commentStep){
        return new JobBuilder("commentJob", jobRepository)
                .start(commentStep)
                .listener(new CustomJobListener(redisTemplate))
                .build();
    }

    @Bean
    @JobScope
    public Step commentStep(JobRepository jobRepository,
                            PlatformTransactionManager platformTransactionManager,
                            FlatFileItemReader<Comment> commentFlatFileItemReader,
                            ItemProcessor<Comment, String> commentItemProcessor,
                            ItemWriter<String> commentItemWriter){
        return new StepBuilder("commentStep", jobRepository)
                .<Comment, String>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(commentFlatFileItemReader)
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .skip(FlatFileParseException.class)
                .processor(commentItemProcessor)
                .writer(commentItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Comment> commentFlatFileItemReader(){
        return new FlatFileItemReaderBuilder<Comment>()
                .name("commentItemReader")
                .linesToSkip(1)
                .resource(new ClassPathResource("comments.csv"))
                .delimited()
                .names("message")
                .targetType(Comment.class)
                .build();
    }


    @Bean
    @StepScope
    public ItemProcessor<Comment, String> commentItemProcessor(){
        return comment -> {
            String message = comment.getMessage();
            String sentence = schoolSentenceFilter(message);
            return sentence;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<String> commentItemWriter() {
        return items -> {
            List<School> schoolAllList = schoolRepository.findAll();
            List<String> schoolNames = items.getItems().stream().map(it -> (String) it).toList();
            for (String schoolName : schoolNames) {
                for (School school : schoolAllList) {
                    if(schoolName.contains(school.getName())){
                        log.info("추출 대상 문장 : " + schoolName + " >>>>> "+" 인증된 학교명 : " + school.getName());
                        redisTemplate.opsForZSet().incrementScore(COUNT_KEY, school.getName(),1 );
                        break;
                    }
                }
            }
        };
    }



    private String schoolSentenceFilter(String message){
        String str = message.replaceAll("[^ㄱ-힣 ]", "");
        if(str.contains("학교")){
            return str;
        }
        return null;
    }
}
