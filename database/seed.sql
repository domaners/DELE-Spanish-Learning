INSERT INTO articles (level_code, title, grammar_focus, summary, body, vocabulary)
VALUES
    (
        'A1',
        'Introducing yourself with ser, llamarse and basic gender',
        'Start with identity, nationality and classroom survival phrases.',
        '',
        'At A1, DELE tasks expect short personal exchanges. Use ser for identity: Soy Ana, soy de Peru. Use llamarse when giving names: Me llamo Luis. Nouns and adjectives agree in gender and number: un amigo simpatico, una amiga simpatica. Build answers in complete but simple sentences.',
        ARRAY['ser', 'llamarse', 'gender agreement', 'nationalities']
    ),
    (
        'A2',
        'Talking about routines with regular present-tense verbs',
        'Describe everyday life, frequency and simple preferences.',
        '',
        'A2 candidates talk about habits and immediate needs. Regular verbs follow predictable endings: hablo, comes, vivimos. Add time markers such as normalmente, todos los dias and por la tarde. Combine routine vocabulary with gustar: Me gusta estudiar por la noche.',
        ARRAY['present tense', 'frequency adverbs', 'gustar', 'daily routines']
    ),
    (
        'B1',
        'Narrating experiences with preterite and imperfect',
        'Tell stories by separating completed events from background context.',
        '',
        'B1 writing and speaking ask you to recount trips, memories and problems. Use the preterite for completed actions: Llegue tarde. Use the imperfect for descriptions, repeated past actions and context: Hacia frio y vivia cerca. Mix both tenses to explain what happened and what was happening.',
        ARRAY['preterite', 'imperfect', 'storytelling', 'past time markers']
    ),
    (
        'B2',
        'Defending opinions with the subjunctive',
        'Move from stating facts to evaluating and recommending actions.',
        '',
        'B2 tasks require argumentation. Use the indicative for certainty: Creo que es util. Use the subjunctive after doubt, emotion, recommendations and impersonal judgement: No creo que sea facil; Es importante que practiques. Connect ideas with aunque, sin embargo and por eso.',
        ARRAY['present subjunctive', 'opinion markers', 'connectors', 'recommendations']
    ),
    (
        'C1',
        'Refining register with passive and impersonal structures',
        'Handle formal texts, reports and nuanced public communication.',
        '',
        'C1 learners need flexible register. Use se pasivo and se impersonal to avoid naming an actor: Se publicaron los resultados; Se vive bien aqui. The periphrastic passive sounds more formal: Los resultados fueron publicados. Choose structures according to text type, audience and emphasis.',
        ARRAY['passive voice', 'impersonal se', 'formal register', 'text cohesion']
    ),
    (
        'C2',
        'Interpreting nuance, idiom and rhetorical stance',
        'Recognize implicit meaning and produce precise, idiomatic Spanish.',
        '',
        'C2 performance depends on nuance. Idioms such as estar en las nubes or dar en el clavo carry cultural meaning. Writers also signal stance through concession, irony and lexical choice. Paraphrase arguments accurately, then respond with precise vocabulary and controlled tone.',
        ARRAY['idioms', 'irony', 'rhetorical stance', 'advanced paraphrase']
    )
ON CONFLICT (title) DO UPDATE
SET level_code = EXCLUDED.level_code,
    grammar_focus = EXCLUDED.grammar_focus,
    summary = EXCLUDED.summary,
    body = EXCLUDED.body,
    vocabulary = EXCLUDED.vocabulary,
    updated_at = now();

INSERT INTO vocabulary_entries (level_code, spanish, english, theme, example)
VALUES
    ('A1', 'hola', 'hello', 'interjection', 'Hola, me llamo Sofia.'),
    ('A1', 'ciudad', 'city', 'noun', 'Madrid es una ciudad grande.'),
    ('A2', 'desayunar', 'to have breakfast', 'verb', 'Desayuno a las ocho.'),
    ('A2', 'cita', 'appointment', 'noun', 'Tengo una cita el martes.'),
    ('B1', 'aunque', 'although/even if', 'connector', 'Aunque llovia, salimos.'),
    ('B1', 'mudarse', 'to move house', 'verb', 'Me mude el ano pasado.'),
    ('B2', 'plantear', 'to raise/propose', 'verb', 'El texto plantea una solucion.'),
    ('B2', 'brecha', 'gap', 'noun', 'La brecha digital afecta al acceso.'),
    ('C1', 'matiz', 'nuance', 'noun', 'Ese argumento tiene varios matices.'),
    ('C1', 'sostener', 'to maintain/argue', 'verb', 'La autora sostiene otra postura.'),
    ('C2', 'desenlace', 'outcome/ending', 'noun', 'El desenlace fue inesperado.'),
    ('C2', 'dar en el clavo', 'to hit the nail on the head', 'idiom', 'Tu respuesta dio en el clavo.')
ON CONFLICT (spanish, english) DO UPDATE
SET level_code = EXCLUDED.level_code,
    theme = EXCLUDED.theme,
    example = EXCLUDED.example,
    updated_at = now();
