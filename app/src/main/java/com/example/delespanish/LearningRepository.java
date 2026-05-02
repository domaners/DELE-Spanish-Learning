package com.example.delespanish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class LearningRepository {
    private final List<Article> articles;
    private final List<VocabularyEntry> vocabulary;
    private final List<VerbConjugation> conjugations;
    private final List<QuizQuestion> placementQuestions;
    private final Map<DeleLevel, List<QuizQuestion>> dailyQuestions;

    LearningRepository() {
        articles = createArticles();
        vocabulary = createVocabulary();
        conjugations = createConjugations();
        placementQuestions = createPlacementQuestions();
        dailyQuestions = createDailyQuestions();
    }

    List<Article> getArticles() {
        return articles;
    }

    List<VocabularyEntry> getVocabulary() {
        return vocabulary;
    }

    List<VerbConjugation> getConjugations() {
        return conjugations;
    }

    List<QuizQuestion> getPlacementQuestions() {
        return placementQuestions;
    }

    List<QuizQuestion> getDailyQuestionsFor(DeleLevel level) {
        List<QuizQuestion> selected = new ArrayList<>();
        for (DeleLevel candidate : DeleLevel.values()) {
            selected.addAll(dailyQuestions.get(candidate));
            if (candidate == level) {
                break;
            }
        }
        return Collections.unmodifiableList(selected);
    }

    List<QuizQuestion> getDailyQuestions(DeleLevel level) {
        return getDailyQuestionsFor(level);
    }

    List<QuizQuestion> getDailyQuestions(DeleLevel level, List<String> incorrectQuestionIds, int maxItems) {
        List<QuizQuestion> available = getDailyQuestionsFor(level);
        int limit = Math.min(Math.max(0, maxItems), available.size());
        if (limit == 0) {
            return Collections.emptyList();
        }

        Map<String, QuizQuestion> availableById = new LinkedHashMap<>();
        for (QuizQuestion question : available) {
            availableById.put(question.getId(), question);
        }

        List<QuizQuestion> reviewItems = new ArrayList<>();
        for (String questionId : incorrectQuestionIds) {
            QuizQuestion question = availableById.get(questionId);
            if (question != null && !reviewItems.contains(question)) {
                reviewItems.add(question);
            }
        }

        List<QuizQuestion> newItems = new ArrayList<>();
        for (QuizQuestion question : available) {
            if (!reviewItems.contains(question)) {
                newItems.add(question);
            }
        }

        List<QuizQuestion> selected = new ArrayList<>();
        int reviewTarget = newItems.isEmpty() ? limit : Math.min(reviewItems.size(), (limit + 1) / 2);
        for (int i = 0; i < reviewTarget; i++) {
            selected.add(reviewItems.get(i));
        }
        for (QuizQuestion question : newItems) {
            if (selected.size() == limit) {
                break;
            }
            selected.add(question);
        }
        for (QuizQuestion question : reviewItems) {
            if (selected.size() == limit) {
                break;
            }
            if (!selected.contains(question)) {
                selected.add(question);
            }
        }
        return Collections.unmodifiableList(selected);
    }

    QuizQuestion getDailyQuestionById(String id) {
        for (List<QuizQuestion> questions : dailyQuestions.values()) {
            for (QuizQuestion question : questions) {
                if (question.getId().equals(id)) {
                    return question;
                }
            }
        }
        return null;
    }

    List<Article> getArticlesFor(DeleLevel level) {
        List<Article> result = new ArrayList<>();
        for (Article article : articles) {
            if (article.getLevel() == level) {
                result.add(article);
            }
        }
        return Collections.unmodifiableList(result);
    }

    List<Article> getArticlesUpTo(DeleLevel level) {
        List<Article> result = new ArrayList<>();
        for (Article article : articles) {
            if (article.getLevel().ordinal() <= level.ordinal()) {
                result.add(article);
            }
        }
        return Collections.unmodifiableList(result);
    }

    List<VocabularyEntry> getVocabularyFor(DeleLevel level) {
        List<VocabularyEntry> result = new ArrayList<>();
        for (VocabularyEntry entry : vocabulary) {
            if (entry.getLevel() == level) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    List<VocabularyEntry> getVocabularyUpTo(DeleLevel level) {
        List<VocabularyEntry> result = new ArrayList<>();
        for (VocabularyEntry entry : vocabulary) {
            if (entry.getLevel().ordinal() <= level.ordinal()) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    List<VerbConjugation> getConjugationsFor(DeleLevel level) {
        List<VerbConjugation> result = new ArrayList<>();
        for (VerbConjugation conjugation : conjugations) {
            if (conjugation.getLevel() == level) {
                result.add(conjugation);
            }
        }
        return Collections.unmodifiableList(result);
    }

    List<VerbConjugation> getVerbsUpTo(DeleLevel level) {
        List<VerbConjugation> result = new ArrayList<>();
        for (VerbConjugation conjugation : conjugations) {
            if (conjugation.getLevel().ordinal() <= level.ordinal()) {
                result.add(conjugation);
            }
        }
        return Collections.unmodifiableList(result);
    }

    DeleLevel recommendLevel(int correctAnswers, int totalQuestions) {
        if (totalQuestions == 0) {
            return DeleLevel.A1;
        }
        double score = (double) correctAnswers / totalQuestions;
        if (score >= 0.9) {
            return DeleLevel.C2;
        }
        if (score >= 0.75) {
            return DeleLevel.C1;
        }
        if (score >= 0.6) {
            return DeleLevel.B2;
        }
        if (score >= 0.45) {
            return DeleLevel.B1;
        }
        if (score >= 0.25) {
            return DeleLevel.A2;
        }
        return DeleLevel.A1;
    }

    String describeReadiness(DeleLevel level, int correctAnswers, int totalQuestions) {
        int percent = totalQuestions == 0 ? 0 : Math.round((correctAnswers * 100f) / totalQuestions);
        return "Placement score: " + percent + "%\n"
                + "Suggested DELE target: " + level.name() + " - " + level.getTitle() + "\n"
                + level.getExamFocus();
    }

    private List<Article> createArticles() {
        List<Article> items = new ArrayList<>();
        items.add(new Article(
                DeleLevel.A1,
                "Introducing yourself with ser, llamarse and basic gender",
                "Start with identity, nationality and classroom survival phrases.",
                "At A1, DELE tasks expect short personal exchanges. Use ser for identity: Soy Ana, soy de Peru. "
                        + "Use llamarse when giving names: Me llamo Luis. Nouns and adjectives agree in gender and number: "
                        + "un amigo simpatico, una amiga simpatica. Build answers in complete but simple sentences.",
                Arrays.asList("ser", "llamarse", "gender agreement", "nationalities")));
        items.add(new Article(
                DeleLevel.A2,
                "Talking about routines with regular present-tense verbs",
                "Describe everyday life, frequency and simple preferences.",
                "A2 candidates talk about habits and immediate needs. Regular verbs follow predictable endings: hablo, comes, vivimos. "
                        + "Add time markers such as normalmente, todos los dias and por la tarde. Combine routine vocabulary with gustar: "
                        + "Me gusta estudiar por la noche.",
                Arrays.asList("present tense", "frequency adverbs", "gustar", "daily routines")));
        items.add(new Article(
                DeleLevel.B1,
                "Narrating experiences with preterite and imperfect",
                "Tell stories by separating completed events from background context.",
                "B1 writing and speaking ask you to recount trips, memories and problems. Use the preterite for completed actions: "
                        + "Llegue tarde. Use the imperfect for descriptions, repeated past actions and context: Hacia frio y vivia cerca. "
                        + "Mix both tenses to explain what happened and what was happening.",
                Arrays.asList("preterite", "imperfect", "storytelling", "past time markers")));
        items.add(new Article(
                DeleLevel.B2,
                "Defending opinions with the subjunctive",
                "Move from stating facts to evaluating and recommending actions.",
                "B2 tasks require argumentation. Use the indicative for certainty: Creo que es util. Use the subjunctive after doubt, "
                        + "emotion, recommendations and impersonal judgement: No creo que sea facil; Es importante que practiques. "
                        + "Connect ideas with aunque, sin embargo and por eso.",
                Arrays.asList("present subjunctive", "opinion markers", "connectors", "recommendations")));
        items.add(new Article(
                DeleLevel.C1,
                "Refining register with passive and impersonal structures",
                "Handle formal texts, reports and nuanced public communication.",
                "C1 learners need flexible register. Use se pasivo and se impersonal to avoid naming an actor: Se publicaron los resultados; "
                        + "Se vive bien aqui. The periphrastic passive sounds more formal: Los resultados fueron publicados. "
                        + "Choose structures according to text type, audience and emphasis.",
                Arrays.asList("passive voice", "impersonal se", "formal register", "text cohesion")));
        items.add(new Article(
                DeleLevel.C2,
                "Interpreting nuance, idiom and rhetorical stance",
                "Recognize implicit meaning and produce precise, idiomatic Spanish.",
                "C2 performance depends on nuance. Idioms such as estar en las nubes or dar en el clavo carry cultural meaning. "
                        + "Writers also signal stance through concession, irony and lexical choice. Paraphrase arguments accurately, "
                        + "then respond with precise vocabulary and controlled tone.",
                Arrays.asList("idioms", "irony", "rhetorical stance", "advanced paraphrase")));
        return Collections.unmodifiableList(items);
    }

    private List<VocabularyEntry> createVocabulary() {
        List<VocabularyEntry> items = new ArrayList<>();
        items.add(new VocabularyEntry(DeleLevel.A1, "hola", "hello", "interjection", "Hola, me llamo Sofia."));
        items.add(new VocabularyEntry(DeleLevel.A1, "ciudad", "city", "noun", "Madrid es una ciudad grande."));
        items.add(new VocabularyEntry(DeleLevel.A2, "desayunar", "to have breakfast", "verb", "Desayuno a las ocho."));
        items.add(new VocabularyEntry(DeleLevel.A2, "cita", "appointment", "noun", "Tengo una cita el martes."));
        items.add(new VocabularyEntry(DeleLevel.B1, "aunque", "although/even if", "connector", "Aunque llovia, salimos."));
        items.add(new VocabularyEntry(DeleLevel.B1, "mudarse", "to move house", "verb", "Me mude el ano pasado."));
        items.add(new VocabularyEntry(DeleLevel.B2, "plantear", "to raise/propose", "verb", "El texto plantea una solucion."));
        items.add(new VocabularyEntry(DeleLevel.B2, "brecha", "gap", "noun", "La brecha digital afecta al acceso."));
        items.add(new VocabularyEntry(DeleLevel.C1, "matiz", "nuance", "noun", "Ese argumento tiene varios matices."));
        items.add(new VocabularyEntry(DeleLevel.C1, "sostener", "to maintain/argue", "verb", "La autora sostiene otra postura."));
        items.add(new VocabularyEntry(DeleLevel.C2, "desenlace", "outcome/ending", "noun", "El desenlace fue inesperado."));
        items.add(new VocabularyEntry(DeleLevel.C2, "dar en el clavo", "to hit the nail on the head", "idiom", "Tu respuesta dio en el clavo."));
        return Collections.unmodifiableList(items);
    }

    private List<VerbConjugation> createConjugations() {
        List<VerbConjugation> items = new ArrayList<>();
        items.add(new VerbConjugation(DeleLevel.A1, "ser", "presente", "to be", mapOf(
                "yo", "soy", "tu", "eres", "el/ella", "es", "nosotros", "somos", "ellos", "son")));
        items.add(new VerbConjugation(DeleLevel.A2, "hablar", "presente", "to speak", mapOf(
                "yo", "hablo", "tu", "hablas", "el/ella", "habla", "nosotros", "hablamos", "ellos", "hablan")));
        items.add(new VerbConjugation(DeleLevel.B1, "tener", "preterito", "to have", mapOf(
                "yo", "tuve", "tu", "tuviste", "el/ella", "tuvo", "nosotros", "tuvimos", "ellos", "tuvieron")));
        items.add(new VerbConjugation(DeleLevel.B2, "practicar", "presente de subjuntivo", "to practise", mapOf(
                "yo", "practique", "tu", "practiques", "el/ella", "practique", "nosotros", "practiquemos", "ellos", "practiquen")));
        items.add(new VerbConjugation(DeleLevel.C1, "publicar", "pasiva perifrastica", "to publish", mapOf(
                "presente", "es publicado", "preterito", "fue publicado", "futuro", "sera publicado", "condicional", "seria publicado")));
        items.add(new VerbConjugation(DeleLevel.C2, "deducir", "subjuntivo imperfecto", "to deduce", mapOf(
                "yo", "dedujera", "tu", "dedujeras", "el/ella", "dedujera", "nosotros", "dedujeramos", "ellos", "dedujeran")));
        return Collections.unmodifiableList(items);
    }

    private List<QuizQuestion> createPlacementQuestions() {
        List<QuizQuestion> items = new ArrayList<>();
        items.add(question(DeleLevel.A1, "Choose the correct introduction.", "Me llamo Carla.", "Me llama Carla.", "Yo llamar Carla.", "Mi llamo Carla.", 0));
        items.add(question(DeleLevel.A1, "Which adjective agrees with 'la profesora'?", "alto", "alta", "altos", "altas", 1));
        items.add(question(DeleLevel.A2, "Complete: Normalmente ___ cafe por la manana.", "bebes", "bebo", "beben", "beber", 1));
        items.add(question(DeleLevel.A2, "What does 'cita' mean in everyday A2 contexts?", "appointment", "kitchen", "cloud", "wallet", 0));
        items.add(question(DeleLevel.B1, "Choose the best past-tense contrast: ___ frio cuando ___ al hotel.", "Hizo / llegaba", "Hacia / llegue", "Hizo / llego", "Hacia / llegaba", 1));
        items.add(question(DeleLevel.B1, "Which connector can mean 'although'?", "sin embargo", "por eso", "aunque", "ademas", 2));
        items.add(question(DeleLevel.B2, "Complete: Es importante que ___ todos los dias.", "practicas", "practicar", "practiques", "practicaste", 2));
        items.add(question(DeleLevel.B2, "Which phrase expresses doubt and triggers subjunctive?", "Creo que", "Es cierto que", "No creo que", "Se que", 2));
        items.add(question(DeleLevel.C1, "Choose the impersonal structure.", "Se vive bien aqui.", "Vive bien aqui Maria.", "Maria vive bien.", "Vivimos aqui.", 0));
        items.add(question(DeleLevel.C1, "Which version is formal passive?", "Publicaron los resultados.", "Los resultados fueron publicados.", "Se publico.", "Alguien publico todo.", 1));
        items.add(question(DeleLevel.C2, "What does 'dar en el clavo' mean?", "to miss the point", "to hit the nail on the head", "to give up", "to improvise", 1));
        items.add(question(DeleLevel.C2, "C2 reading often tests the author's implicit...", "shoe size", "rhetorical stance", "alphabet", "timetable", 1));
        return Collections.unmodifiableList(items);
    }

    private Map<DeleLevel, List<QuizQuestion>> createDailyQuestions() {
        Map<DeleLevel, List<QuizQuestion>> items = new EnumMap<>(DeleLevel.class);
        for (DeleLevel level : DeleLevel.values()) {
            items.put(level, new ArrayList<QuizQuestion>());
        }
        items.get(DeleLevel.A1).add(question(DeleLevel.A1, "Which sentence uses ser for identity?", "Estoy Ana.", "Soy Ana.", "Tengo Ana.", "Hay Ana.", 1));
        items.get(DeleLevel.A1).add(question(DeleLevel.A1, "Translate 'city'.", "ciudad", "cita", "casa", "clase", 0));
        items.get(DeleLevel.A2).add(question(DeleLevel.A2, "Complete: Me gusta ___ por la noche.", "estudio", "estudiar", "estudie", "estudiaba", 1));
        items.get(DeleLevel.A2).add(question(DeleLevel.A2, "Which marker describes frequency?", "ayer", "normalmente", "de repente", "quiza", 1));
        items.get(DeleLevel.B1).add(question(DeleLevel.B1, "Which tense gives background description?", "imperfect", "future", "imperative", "conditional perfect", 0));
        items.get(DeleLevel.B1).add(question(DeleLevel.B1, "Choose the preterite of tener for ellos.", "tenian", "tendran", "tuvieron", "tengan", 2));
        items.get(DeleLevel.B2).add(question(DeleLevel.B2, "Complete: No creo que ___ facil.", "es", "sea", "fue", "sera", 1));
        items.get(DeleLevel.B2).add(question(DeleLevel.B2, "Which word means 'gap'?", "brecha", "matiz", "desenlace", "cita", 0));
        items.get(DeleLevel.C1).add(question(DeleLevel.C1, "Which structure avoids naming the actor?", "impersonal se", "present progressive only", "direct object", "possessive adjective", 0));
        items.get(DeleLevel.C1).add(question(DeleLevel.C1, "What does 'matiz' mean?", "nuance", "schedule", "breakfast", "mistake", 0));
        items.get(DeleLevel.C2).add(question(DeleLevel.C2, "Idioms often carry...", "only spelling rules", "cultural meaning", "no context", "basic gender", 1));
        items.get(DeleLevel.C2).add(question(DeleLevel.C2, "A precise C2 response should control vocabulary and...", "tone", "font size", "screen width", "battery", 0));
        for (Map.Entry<DeleLevel, List<QuizQuestion>> entry : items.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(items);
    }

    private QuizQuestion question(DeleLevel level, String prompt, String a, String b, String c, String d, int correctAnswerIndex) {
        return new QuizQuestion(level, prompt, Arrays.asList(a, b, c, d), correctAnswerIndex);
    }

    private Map<String, String> mapOf(String... values) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }
}
