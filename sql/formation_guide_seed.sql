USE campconnect;

SET @seed_user_email = 'admin123@campconnect.com';
SET @seed_user_id = (
    SELECT id
    FROM utilisateur
    WHERE email = @seed_user_email
    LIMIT 1
);

-- Verify the target user exists before executing inserts.
SELECT @seed_user_id AS seed_user_id;

INSERT INTO formation (
    titre,
    description,
    date_creation,
    status,
    level,
    duration,
    guide_id,
    auteur_email,
    auteur_nom
) VALUES (
    'Seed Formation Guide Interactif',
    'Formation seedee pour tester Formation + GuideInteractif.',
    NOW(6),
    'DRAFT',
    'BEGINNER',
    60,
    @seed_user_id,
    @seed_user_email,
    'CampConnect'
);

SET @seed_formation_id = LAST_INSERT_ID();

INSERT INTO formation_media (
    formation_id,
    media_type,
    media_url,
    media_public_id,
    file_name,
    mime_type,
    file_size,
    display_order,
    upload_date
) VALUES (
    @seed_formation_id,
    'IMAGE',
    'https://example.com/seed-formation-image.jpg',
    CONCAT('seed-formation-', @seed_formation_id),
    'seed-formation-image.jpg',
    'image/jpeg',
    1024,
    0,
    NOW(6)
);

INSERT INTO guide_interactif (
    formation_id,
    titre,
    description,
    recompense_finale,
    created_at
) VALUES (
    @seed_formation_id,
    'Guide Interactif Seed',
    'Guide de demonstration pour la progression utilisateur.',
    'Bravo, parcours termine.',
    NOW(6)
);

SET @seed_guide_id = LAST_INSERT_ID();

INSERT INTO guide_interactif_step (
    guide_id,
    step_order,
    titre,
    description,
    media_type,
    media_url,
    checklist
) VALUES
(
    @seed_guide_id,
    1,
    'Etape 1',
    'Verifier le materiel de base.',
    'IMAGE',
    'https://example.com/guide-step-1.jpg',
    'sac, eau, trousse'
),
(
    @seed_guide_id,
    2,
    'Etape 2',
    'Monter le camp en securite.',
    'VIDEO',
    'https://example.com/guide-step-2.mp4',
    'tente, fixation, securite'
);

INSERT INTO guide_progress_user (
    guide_id,
    utilisateur_id,
    total_steps,
    completed_steps,
    progress_percent,
    completed,
    reward_unlocked,
    reward_unlocked_at,
    last_updated
) VALUES (
    @seed_guide_id,
    @seed_user_id,
    2,
    2,
    100,
    b'1',
    b'1',
    NOW(6),
    NOW(6)
);

INSERT INTO user_reward (
    guide_id,
    utilisateur_id,
    badge,
    points,
    bonus,
    awarded_at
) VALUES (
    @seed_guide_id,
    @seed_user_id,
    'Explorateur du camping',
    50,
    'Template bonus: checklist complete d''organisation d''un camping reussi.',
    NOW(6)
);

SELECT
    @seed_formation_id AS seed_formation_id,
    @seed_guide_id AS seed_guide_id;
