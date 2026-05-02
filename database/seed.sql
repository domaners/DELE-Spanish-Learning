INSERT INTO articles (level, title, grammar_focus, summary, body, vocabulary) VALUES
('A1', 'Introducing yourself with ser, llamarse and basic gender', 'Identity, nationality and classroom survival phrases.', 'Start with identity, nationality and classroom survival phrases.', 'At A1, DELE tasks expect short personal exchanges. Use ser for identity: Soy Ana, soy de Peru. Use llamarse when giving names: Me llamo Luis. Nouns and adjectives agree in gender and number: un amigo simpatico, una amiga simpatica. Build answers in complete but simple sentences.', ARRAY['ser', 'llamarse', 'gender agreement', 'nationalities']),
('A2', 'Talking about routines with regular present-tense verbs', 'Routines, frequency and simple preferences.', 'Describe everyday life, frequency and simple preferences.', 'A2 candidates talk about habits and immediate needs. Regular verbs follow predictable endings: hablo, comes, vivimos. Add time markers such as normalmente, todos los dias and por la tarde. Combine routine vocabulary with gustar: Me gusta estudiar por la noche.', ARRAY['present tense', 'frequency adverbs', 'gustar', 'daily routines']),
('B1', 'Narrating experiences with preterite and imperfect', 'Completed events versus background context.', 'Tell stories by separating completed events from background context.', 'B1 writing and speaking ask you to recount trips, memories and problems. Use the preterite for completed actions: Llegue tarde. Use the imperfect for descriptions, repeated past actions and context: Hacia frio y vivia cerca. Mix both tenses to explain what happened and what was happening.', ARRAY['preterite', 'imperfect', 'storytelling', 'past time markers']),
('B2', 'Defending opinions with the subjunctive', 'Argumentation, doubt, recommendations and evaluation.', 'Move from stating facts to evaluating and recommending actions.', 'B2 tasks require argumentation. Use the indicative for certainty: Creo que es util. Use the subjunctive after doubt, emotion, recommendations and impersonal judgement: No creo que sea facil; Es importante que practiques. Connect ideas with aunque, sin embargo and por eso.', ARRAY['present subjunctive', 'opinion markers', 'connectors', 'recommendations']),
('C1', 'Refining register with passive and impersonal structures', 'Formal texts, reports and nuanced public communication.', 'Handle formal texts, reports and nuanced public communication.', 'C1 learners need flexible register. Use se pasivo and se impersonal to avoid naming an actor: Se publicaron los resultados; Se vive bien aqui. The periphrastic passive sounds more formal: Los resultados fueron publicados. Choose structures according to text type, audience and emphasis.', ARRAY['passive voice', 'impersonal se', 'formal register', 'text cohesion']),
('C2', 'Interpreting nuance, idiom and rhetorical stance', 'Implicit meaning, idiom, irony and precise tone.', 'Recognize implicit meaning and produce precise, idiomatic Spanish.', 'C2 performance depends on nuance. Idioms such as estar en las nubes or dar en el clavo carry cultural meaning. Writers also signal stance through concession, irony and lexical choice. Paraphrase arguments accurately, then respond with precise vocabulary and controlled tone.', ARRAY['idioms', 'irony', 'rhetorical stance', 'advanced paraphrase']);

INSERT INTO vocabulary_entries (level, spanish, english, theme, example) VALUES
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
('C2', 'dar en el clavo', 'to hit the nail on the head', 'idiom', 'Tu respuesta dio en el clavo.');

INSERT INTO verb_conjugations (level, infinitive, tense, meaning, forms) VALUES
('A1', 'ser', 'presente', 'to be', '{"yo":"soy","tu":"eres","el/ella":"es","nosotros":"somos","ellos":"son"}'),
('A2', 'hablar', 'presente', 'to speak', '{"yo":"hablo","tu":"hablas","el/ella":"habla","nosotros":"hablamos","ellos":"hablan"}'),
('B1', 'tener', 'preterito', 'to have', '{"yo":"tuve","tu":"tuviste","el/ella":"tuvo","nosotros":"tuvimos","ellos":"tuvieron"}'),
('B2', 'practicar', 'presente de subjuntivo', 'to practise', '{"yo":"practique","tu":"practiques","el/ella":"practique","nosotros":"practiquemos","ellos":"practiquen"}'),
('C1', 'publicar', 'pasiva perifrastica', 'to publish', '{"presente":"es publicado","preterito":"fue publicado","futuro":"sera publicado","condicional":"seria publicado"}'),
('C2', 'deducir', 'subjuntivo imperfecto', 'to deduce', '{"yo":"dedujera","tu":"dedujeras","el/ella":"dedujera","nosotros":"dedujeramos","ellos":"dedujeran"}');

INSERT INTO quiz_questions (quiz_type, level, prompt, options, correct_answer_index) VALUES
('placement', 'A1', 'Choose the correct introduction.', ARRAY['Me llamo Carla.', 'Me llama Carla.', 'Yo llamar Carla.', 'Mi llamo Carla.'], 0),
('placement', 'A1', 'Which adjective agrees with ''la profesora''?', ARRAY['alto', 'alta', 'altos', 'altas'], 1),
('placement', 'A2', 'Complete: Normalmente ___ cafe por la manana.', ARRAY['bebes', 'bebo', 'beben', 'beber'], 1),
('placement', 'A2', 'What does ''cita'' mean in everyday A2 contexts?', ARRAY['appointment', 'kitchen', 'cloud', 'wallet'], 0),
('placement', 'B1', 'Choose the best past-tense contrast: ___ frio cuando ___ al hotel.', ARRAY['Hizo / llegaba', 'Hacia / llegue', 'Hizo / llego', 'Hacia / llegaba'], 1),
('placement', 'B1', 'Which connector can mean ''although''?', ARRAY['sin embargo', 'por eso', 'aunque', 'ademas'], 2),
('placement', 'B2', 'Complete: Es importante que ___ todos los dias.', ARRAY['practicas', 'practicar', 'practiques', 'practicaste'], 2),
('placement', 'B2', 'Which phrase expresses doubt and triggers subjunctive?', ARRAY['Creo que', 'Es cierto que', 'No creo que', 'Se que'], 2),
('placement', 'C1', 'Choose the impersonal structure.', ARRAY['Se vive bien aqui.', 'Vive bien aqui Maria.', 'Maria vive bien.', 'Vivimos aqui.'], 0),
('placement', 'C1', 'Which version is formal passive?', ARRAY['Publicaron los resultados.', 'Los resultados fueron publicados.', 'Se publico.', 'Alguien publico todo.'], 1),
('placement', 'C2', 'What does ''dar en el clavo'' mean?', ARRAY['to miss the point', 'to hit the nail on the head', 'to give up', 'to improvise'], 1),
('placement', 'C2', 'C2 reading often tests the author''s implicit...', ARRAY['shoe size', 'rhetorical stance', 'alphabet', 'timetable'], 1),
('daily', 'A1', 'Which sentence uses ser for identity?', ARRAY['Estoy Ana.', 'Soy Ana.', 'Tengo Ana.', 'Hay Ana.'], 1),
('daily', 'A1', 'Translate ''city''.', ARRAY['ciudad', 'cita', 'casa', 'clase'], 0),
('daily', 'A2', 'Complete: Me gusta ___ por la noche.', ARRAY['estudio', 'estudiar', 'estudie', 'estudiaba'], 1),
('daily', 'A2', 'Which marker describes frequency?', ARRAY['ayer', 'normalmente', 'de repente', 'quiza'], 1),
('daily', 'B1', 'Which tense gives background description?', ARRAY['imperfect', 'future', 'imperative', 'conditional perfect'], 0),
('daily', 'B1', 'Choose the preterite of tener for ellos.', ARRAY['tenian', 'tendran', 'tuvieron', 'tengan'], 2),
('daily', 'B2', 'Complete: No creo que ___ facil.', ARRAY['es', 'sea', 'fue', 'sera'], 1),
('daily', 'B2', 'Which word means ''gap''?', ARRAY['brecha', 'matiz', 'desenlace', 'cita'], 0),
('daily', 'C1', 'Which structure avoids naming the actor?', ARRAY['impersonal se', 'present progressive only', 'direct object', 'possessive adjective'], 0),
('daily', 'C1', 'What does ''matiz'' mean?', ARRAY['nuance', 'schedule', 'breakfast', 'mistake'], 0),
('daily', 'C2', 'Idioms often carry...', ARRAY['only spelling rules', 'cultural meaning', 'no context', 'basic gender'], 1),
('daily', 'C2', 'A precise C2 response should control vocabulary and...', ARRAY['tone', 'font size', 'screen width', 'battery'], 0);
