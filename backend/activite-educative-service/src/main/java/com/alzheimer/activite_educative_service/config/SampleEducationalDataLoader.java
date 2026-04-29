package com.alzheimer.activite_educative_service.config;

import com.alzheimer.activite_educative_service.dto.QuestionRequest;
import com.alzheimer.activite_educative_service.entities.*;
import com.alzheimer.activite_educative_service.repositories.ActiviteEducativeRepository;
import com.alzheimer.activite_educative_service.services.EducationalQuestionService;
import com.alzheimer.activite_educative_service.services.PreconfiguredQuestionSeedService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Données d’exemple pour Memory Quiz, Image Recognition, Memory paires, et quiz image (profil par défaut).
 */
@Component
@Profile("!test")
public class SampleEducationalDataLoader implements CommandLineRunner {

    private static final String WIKI = "https://upload.wikimedia.org/wikipedia/commons/thumb/";
    private static final String ORANGE_IMG =
            WIKI + "c/c4/Orange-Fruit-Pieces.jpg/320px-Orange-Fruit-Pieces.jpg";
    private static final String QUIZ_THUMB =
            WIKI + "f/f3/Brockhaus_and_Efron_Dictionary_B82_765-2.jpg/320px-Brockhaus_and_Efron_Dictionary_B82_765-2.jpg";
    private static final String IMAGE_RECOG_THUMB =
            WIKI + "2/20/Swiss_National_Park_093.JPG/360px-Swiss_National_Park_093.JPG";
    private static final String CONTENT_THUMB =
            WIKI + "6/6f/Brussels_sprouts_closeup.jpg/320px-Brussels_sprouts_closeup.jpg";

    private final ActiviteEducativeRepository activiteEducativeRepository;
    private final PreconfiguredQuestionSeedService preconfiguredQuestionSeedService;
    private final EducationalQuestionService educationalQuestionService;

    public SampleEducationalDataLoader(
            ActiviteEducativeRepository activiteEducativeRepository,
            PreconfiguredQuestionSeedService preconfiguredQuestionSeedService,
            EducationalQuestionService educationalQuestionService
    ) {
        this.activiteEducativeRepository = activiteEducativeRepository;
        this.preconfiguredQuestionSeedService = preconfiguredQuestionSeedService;
        this.educationalQuestionService = educationalQuestionService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (activiteEducativeRepository.count() > 0) {
            return;
        }

        ActiviteEducative memory = new ActiviteEducative();
        memory.setTitle("Memory Quiz");
        memory.setDescription("Quiz mémoire sur des mots et associations courtes.");
        memory.setType(ActivityType.GAME);
        memory.setGameType(GameType.MEMORY_QUIZ);
        memory.setIconKey("brain");
        memory.setStatus(ActivityStatus.ACTIVE);
        memory.setScoreThreshold(60.0);
        memory.setThumbnailUrl(QUIZ_THUMB);
        memory = activiteEducativeRepository.save(memory);

        ActiviteEducative image = new ActiviteEducative();
        image.setTitle("Image Recognition");
        image.setDescription("Reconnaître des objets à partir d’images.");
        image.setType(ActivityType.GAME);
        image.setGameType(GameType.IMAGE_RECOGNITION);
        image.setIconKey("image");
        image.setStatus(ActivityStatus.ACTIVE);
        image.setScoreThreshold(70.0);
        image.setThumbnailUrl(IMAGE_RECOG_THUMB);
        image = activiteEducativeRepository.save(image);

        ActiviteEducative diet = new ActiviteEducative();
        diet.setTitle("Healthy Diet");
        diet.setDescription("Conseils nutritionnels et contenus à lire.");
        diet.setType(ActivityType.CONTENT);
        diet.setIconKey("book");
        diet.setStatus(ActivityStatus.ACTIVE);
        diet.setThumbnailUrl(CONTENT_THUMB);
        activiteEducativeRepository.save(diet);

        ActiviteEducative memMatch = new ActiviteEducative();
        memMatch.setTitle("Memory — paires d’images");
        memMatch.setDescription("Retournez deux cartes identiques pour former une paire.");
        memMatch.setType(ActivityType.GAME);
        memMatch.setGameType(GameType.MEMORY_MATCH);
        memMatch.setIconKey("image");
        memMatch.setStatus(ActivityStatus.ACTIVE);
        memMatch.setScoreThreshold(100.0);
        memMatch.setThumbnailUrl(
                WIKI + "4/43/Bonnet_macaque_%28Macaca_radiata%29.jpg/320px-Bonnet_macaque_%28Macaca_radiata%29.jpg");
        memMatch = activiteEducativeRepository.save(memMatch);

        ActiviteEducative fruitQuiz = new ActiviteEducative();
        fruitQuiz.setTitle("Quiz image — quel fruit ?");
        fruitQuiz.setDescription("Une question avec image : reconnaître le fruit.");
        fruitQuiz.setType(ActivityType.GAME);
        fruitQuiz.setGameType(GameType.IMAGE_RECOGNITION);
        fruitQuiz.setIconKey("image");
        fruitQuiz.setStatus(ActivityStatus.ACTIVE);
        fruitQuiz.setScoreThreshold(60.0);
        fruitQuiz.setThumbnailUrl(ORANGE_IMG);
        fruitQuiz = activiteEducativeRepository.save(fruitQuiz);

        preconfiguredQuestionSeedService.ensureDefaultQuestions(memory);
        preconfiguredQuestionSeedService.ensureDefaultQuestions(image);
        preconfiguredQuestionSeedService.ensureDefaultQuestions(memMatch);

        QuestionRequest orangeQ = new QuestionRequest();
        orangeQ.setOrderIndex(1);
        orangeQ.setPrompt("Quel fruit vois-tu ?");
        orangeQ.setImageUrl(ORANGE_IMG);
        orangeQ.setOptions(List.of("Orange", "Banane", "Pomme", "Cerise"));
        orangeQ.setCorrectAnswer("Orange");
        educationalQuestionService.createQuestion(fruitQuiz.getId(), orangeQ);

        QuestionRequest appleQ = new QuestionRequest();
        appleQ.setOrderIndex(2);
        appleQ.setPrompt("Quel fruit rouge ou vert est souvent croqué ?");
        appleQ.setImageUrl(
                WIKI + "1/15/Red_Apple.jpg/200px-Red_Apple.jpg");
        appleQ.setOptions(List.of("Pomme", "Tomate", "Cerise", "Radis"));
        appleQ.setCorrectAnswer("Pomme");
        educationalQuestionService.createQuestion(fruitQuiz.getId(), appleQ);
    }
}
