package com.project.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

//CLASS INI ISINYA PROSES-PROSES PAS GAMENYA LAGI JALAN/IN GAME (THE LOGIC)
public class InGame implements Screen
{
    final Shooter game;

    private final Texture playerImg,asteroidImg,laserImg,smallEnemyImg, mediumEnemyImg, enemyLaserImg,bossImg,projectileImg, backgroundImg;
    private final OrthographicCamera camera;
    final Music inGameMusic;
    final Sound explosionSound,laserSound,projectileSound;
    private final BitmapFont font;

    Player player;
    Rectangle laser;
    Enemy a;                                                    //a ini enemy sementara buat ngisi array musuh
    Array<Rectangle> lasers;
    Array<Rectangle> enemyLasers;
    Array<Rectangle> projectiles;
    Array<Enemy> asteroids;
    Array<Enemy> enemies;

    long lastAttackTime, lastProjectileTime, lastAsteroidTime, lastSpawnTime, lastPlayerCrashTime, lastEnemyAttackTime;
    int enemyDestroyed, touchSide, i, count, random;
    double scoreMultiplier;
    boolean bossState;                                          //maksudnya state sekarang lagi boss fight atau tidak

    public InGame(Shooter game)
    {
        this.game = game;

        // assets manual
        playerImg = new Texture("player.png");
        smallEnemyImg = new Texture("smallEnemy.png");
        mediumEnemyImg = new Texture("mediumEnemy.png");
        bossImg = new Texture("boss.png");
        laserImg = new Texture("laser.png");
        enemyLaserImg = new Texture("enemyLaser.png");
        asteroidImg = new Texture("asteroid.png");
        projectileImg = new Texture("projectile.png");
        backgroundImg = new Texture("background.png");
        font = new BitmapFont(Gdx.files.internal("Title.fnt")); // use arial
        inGameMusic = Gdx.audio.newMusic(Gdx.files.internal("inGame.mp3"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));
        laserSound = Gdx.audio.newSound(Gdx.files.internal("laserSound.mp3"));
        projectileSound = Gdx.audio.newSound(Gdx.files.internal("projectileSound.mp3"));


        // assets menggunakan class Assets
//        playerImg = assetManager.get(Assets.playerImg);
//        enemyImg = assetManager.get(Assets.enemyImg);
//        bossImg = assetManager.get(Assets.bossImg);
//        laserImg = assetManager.get(Assets.laserImg);
//        asteroidImg = assetManager.get(Assets.asteroidImg);
//        projectileImg = assetManager.get(Assets.projectileImg);
//        backgroundImg = assetManager.get(Assets.backgroundImg);
//        font = assetManager.get(Assets.menuFont); // use the title font
        font.getData().setScale(0.2f);//set size for the font

        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,950);

        player = new Player();
        player.x = 400-32;
        player.y = 40;
        player.width = 64;
        player.height = 64;

        lasers = new Array<Rectangle>();
        spawnLaserPulse();

        enemyLasers = new Array<Rectangle>();

        projectiles = new Array<Rectangle>();

        asteroids = new Array<Enemy>();
        spawnAsteroids();

        enemies = new Array<Enemy>();
        i = 0;
        getEnemyBatch(i);
        i++;

        bossState = false;

        enemyDestroyed = 0;
    }

    //fungsi show bakal insert lagu pas awal mulai
    @Override
    public void show()
    {
        inGameMusic.setVolume(1f);
        inGameMusic.play();
        inGameMusic.setLooping(true);
    }

    @Override
    public void render(float delta)
    {
        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();

        //==================================================================GAMBAR OBJECT-OBJECT=================================================================================

        game.batch.begin();
        game.batch.draw(backgroundImg,0,0);
        game.batch.draw(playerImg, player.x, player.y);
        for (Rectangle laser : lasers)
        {
            game.batch.draw(laserImg, laser.x, laser.y);
        }
        for (Rectangle laser : enemyLasers)
        {
            game.batch.draw(enemyLaserImg, laser.x, laser.y);
        }
        for (Rectangle projectile : projectiles)
        {
            game.batch.draw(projectileImg, projectile.x, projectile.y);
        }
        for (Enemy asteroid : asteroids)
        {
            game.batch.draw(asteroidImg, asteroid.x, asteroid.y);
        }

        //cek apakah objeknya boss/small enemy, gambar sesuai classnya
        for(Enemy enemy : enemies)
        {
            if(enemy instanceof SmallEnemy)
                game.batch.draw(smallEnemyImg, enemy.x, enemy.y);
            else if(enemy instanceof MediumEnemy)
            {
                game.batch.draw(mediumEnemyImg, enemy.x, enemy.y);
            }
            else if(enemy instanceof Boss)
            {
                game.batch.draw(bossImg, enemy.x, enemy.y);
                font.draw(game.batch, "Enemy HP: " + enemies.get(0).getHP(), 400 - 32, 600 - 16);
            }

            //random tembak laser
            if(enemy instanceof MediumEnemy && TimeUtils.nanoTime() - lastEnemyAttackTime > 1500_000_000)
            {
                random = MathUtils.random(1, 10);
                if(random == 2 || random == 5 || random == 9)
                {
                    spawnEnemyLaserPulse(enemy.x, enemy.y);
                }
            }
        }

        font.draw(game.batch, "Player HP: " + player.getHp(), 400-32, 32);
        font.draw(game.batch, "Score: " + player.getScore(), 10, 740);
        game.batch.end();

        //==================================================================SPAWN OBJECT-OBJECT==================================================================================

        move();

        if(bossState)
        {
            bossMove();
            //spawn peluru boss / detik ketika HP > 0
            if (TimeUtils.nanoTime() - lastProjectileTime > 1000_000_000 && enemies.get(0).getHP() > 0)
            {
                spawnProjectile();
                projectileSound.play();
            }
            else if (enemies.get(0).getHP() <= 0){
                enemies.removeIndex(0);
                bossState = false;
                enemyDestroyed++;
                i = 0;
                explosionSound.play();
            }
        }
        else
        {
            if (TimeUtils.nanoTime() - lastSpawnTime > TimeUtils.millisToNanos(15000) || enemies.size == 0)   //waktu untuk ganti wave
            {
                getEnemyBatch(i);
                i++;
            }
        }

        //spawn asteroid / detik
        if (TimeUtils.nanoTime() - lastAsteroidTime > 1000_000_000)
            spawnAsteroids();

        if (TimeUtils.nanoTime() - lastAttackTime > 1000_000_000)
        {
            spawnLaserPulse();
            laserSound.play();
        }

        //=====================================================================COLLISION DETECTION===============================================================================

        //peluru player jalan keatas
        Iterator<Rectangle> iterLaser = lasers.iterator();
        while(iterLaser.hasNext())
        {
            Rectangle laser = iterLaser.next();
            laser.y += 300 * Gdx.graphics.getDeltaTime();
            if (laser.y + 28 > 950)
            {
                if(lasers.size > 0)
                {
                    iterLaser.remove();
                }
            }

            // utk boss tp msh bekerja jg utk musuh biasa
            if (bossState) // batch 11
            {
                if(laser.overlaps(enemies.get(0)) && enemies.get(0).getHP() > 0)
                {
                    if(lasers.size > 0)
                    {
                        iterLaser.remove();
                    }
                    enemies.get(0).menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                }
            }
            else
            {
                count = 0;
                for(Enemy enemy : enemies)
                {
                    if(laser.overlaps(enemy))
                    {
                        if(lasers.size > 0)
                        {
                            iterLaser.remove();
                        }
                        enemy.menerimadamage(player.getLaserDmg(), player, scoreMultiplier);
                        if(enemy.getHP() <= 0)
                        {
                            explosionSound.play();
                            enemies.removeIndex(count);
                        }
                    }

                    if (player.overlaps(enemy))
                    {
                        explosionSound.play();
                        player.menerimadamage(enemy.getDamage());
                        enemies.removeIndex(count);
                    }
                    count++;
                }
            }
        }

        //bagian untuk laser musuh
        Iterator<Rectangle> iterEnemyLaser = enemyLasers.iterator();
        while(iterEnemyLaser.hasNext())
        {
            Rectangle enemyLaser = iterEnemyLaser.next();
            enemyLaser.y -= 300 * Gdx.graphics.getDeltaTime();
            if(enemyLaser.y < 0)
            {
                iterEnemyLaser.remove();
            }
            if (enemyLaser.overlaps(player))
            {
                iterEnemyLaser.remove();
                player.menerimadamage(5); // edit baru
            }
        }

        //peluru boss jalan ke bawah
        Iterator<Rectangle> iterBossProjectile = projectiles.iterator();
        while(iterBossProjectile.hasNext())
        {
            Rectangle projectile = iterBossProjectile.next();
            projectile.y -= 300 * Gdx.graphics.getDeltaTime();
            if(projectile.y < 0)
            {
                iterBossProjectile.remove();
            }
            if (projectile.overlaps(player))
            {
                iterBossProjectile.remove();
                player.menerimadamage(enemies.get(0).getDamage()); // edit baru
            }
        }

        //asteroid jatuh ke bawah
        Iterator<Enemy> iterAsteroid = asteroids.iterator();
        while (iterAsteroid.hasNext())
        {
            Enemy asteroid = iterAsteroid.next();
            asteroid.y -= 300 * Gdx.graphics.getDeltaTime();
            if (asteroid.y < 0)
            {
                iterAsteroid.remove();
            }
            if (asteroid.overlaps(player))
            {
                iterAsteroid.remove();
                player.menerimadamage(asteroid.getDamage());
            }
        }

        // player collision with boss check
        if (bossState)
        {
            if (player.overlaps(enemies.get(0)) && TimeUtils.nanoTime() - lastPlayerCrashTime > 3000_000_000L)
            {
                lastPlayerCrashTime = TimeUtils.nanoTime();
                player.menerimadamage(30);
            }
        }

        //Game Over
        if(player.getHp() <= 0)
        {
            inGameMusic.stop();
            Save.gd.setYourScore(player.getScore());
            game.setScreen(new GameOverScreen(game));
            dispose();
        }


    }

    //=========================================================================FUNGSI-FUNGSI BUATAN KITA=========================================================================

    //gerakan player pake wasd
    private void move()
    {
        if (player.x <= 0)
            player.x = 0;

        if (player.x >= 800 - 64)
            player.x = 800-64;

        if (player.y <= 0)
            player.y = 0;

        if (player.y >= 950 - 64)
            player.y = 950 - 64;

        if (Gdx.input.isKeyPressed(Input.Keys.A))
        {
            player.x -= 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D))
        {
            player.x += 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S))
        {
            player.y -= 300 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W))
        {
            player.y += 300 * Gdx.graphics.getDeltaTime();
        }

        //PENCET SPASI BUAT TAMBAH SCORE 100 BUAT SPAWN BOSS
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
        {
            getEnemyBatch(11);
        }
    }

    //fungsi spawn bossnya (batch musuh nomor 11 itu isinya boss tok)
    private void spawnBoss()
    {
        getEnemyBatch(11);
    }

    //gerakan kanan kiri boss
    private void bossMove()
    {
        if(enemies.get(0).x < 0)
        {
            enemies.get(0).x = 1;
            touchSide++;
        }

        else if (enemies.get(0).x > 800-200)
        {
            enemies.get(0).x = 800-199;
            touchSide++;
        }

        if(touchSide % 2 == 0)
        {
            enemies.get(0).x += 100 * Gdx.graphics.getDeltaTime();
        }
        else
        {
            enemies.get(0).x -= 100 * Gdx.graphics.getDeltaTime();
        }
    }

    //spawn laser pulse dari player
    private void spawnLaserPulse()
    {
        laser = new Rectangle();
        laser.x = player.x + 28;
        laser.y = player.y + 64;
        laser.width = 8;
        laser.height = 28;
        lasers.add(laser);
        lastAttackTime = TimeUtils.nanoTime();
    }

    private void spawnEnemyLaserPulse(float x, float y)
    {
        laser = new Rectangle();
        laser.x = x + 28;
        laser.y = y - 64;
        laser.width = 8;
        laser.height = 28;
        enemyLasers.add(laser);
        lastEnemyAttackTime = TimeUtils.nanoTime();
    }

    //spawn asteroid random
    private void spawnAsteroids()
    {
        Enemy asteroid = new Asteroid();
        asteroid.width = 32;
        asteroid.height = 32;
        asteroid.x = MathUtils.random(0, 800-32);
        asteroid.y = 950 - 32;
        asteroids.add(asteroid);
        lastAsteroidTime = TimeUtils.nanoTime();
    }

    //spawn laser boss
    private void spawnProjectile()
    {
        Rectangle projectile = new Rectangle();
        projectile.width = 64;
        projectile.height = 64;
        projectile.x = enemies.get(0).x + projectile.width+6;
        projectile.y = enemies.get(0).y - projectile.height;
        projectiles.add(projectile);
        lastProjectileTime = TimeUtils.nanoTime();
    }

    //fungsi buat ganti batch/wave musuh
    //musuh"nya masih diisi manual untuk sekarang
    private void getEnemyBatch(int i)
    {
        enemies.clear();
        if(i == 0)          //selesai
        {
            /*
            FORMASI
            00000000000
            00000000000
            01000000010
            00000000000
            00000000000
            */
            a = new SmallEnemy();
            a.x = 204;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 496;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 1;
        }
        else if(i == 1)     //selesai
        {
            /*
            FORMASI
            00100000100
            00000000000
            00000000000
            00000100000
            00000000000
            */
            a = new SmallEnemy();
            a.x = 176;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 1;
        }
        else if(i == 2)     //selesai
        {
            /*
            FORMASI
            00000000000
            00000200000
            01000000010
            00000100000
            00000000000
            */
            a = new MediumEnemy();
            a.x = 368;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 112;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 1.5;
        }
        else if(i == 3)     //selesai
        {
            /*
            FORMASI
            00000000000
            01000100010
            00020002000
            00000000000
            00000000000
            */
            a = new SmallEnemy();
            a.x = 112;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 240;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 496;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 2;
        }
        else if(i == 4)     //selesai
        {
            /*
            FORMASI
            00000100000
            00000000000
            00020002000
            00000000000
            01000000010
            */
            a = new MediumEnemy();
            a.x = 240;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 496;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 112;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 2.5;
        }
        else if(i == 5)     //selesai
        {
            /*
            FORMASI
            000000000
            002010200
            000101000
            000010000
            000000000
            */
            a = new SmallEnemy();
            a.x = 368;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 304;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 432;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 240;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 490;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 3;
        }
        else if(i == 6)     //selesai
        {
            /*
            FORMASI
            00000000000
            02000000020
            00001010000
            00100000100
            00000000000
            */
            a = new MediumEnemy();
            a.x = 112;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 624;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 304;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 432;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 176;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 3.5;
        }
        else if(i == 7)     //selesai
        {
            /*
            FORMASI
            00000000002
            00000000100
            00001010000
            00100000000
            20000000000
            */
            a = new MediumEnemy();
            a.x = 688;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 432;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 304;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 176;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 48;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 4;
        }
        else if(i == 8)     //selesai
        {
            /*
            FORMASI
            00000000000
            00100000100
            20000200002
            00100000100
            00000000000
            */
            a = new SmallEnemy();
            a.x = 176;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 48;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 368;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 688;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 176;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 4.5;
        }
        else if(i == 9)     //selesai
        {
            /*
            FORMASI
            00000200000
            00002020000
            00010001000
            00101010100
            00000000000
            */
            a = new MediumEnemy();
            a.x = 368;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 304;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 432;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 240;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 496;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 560;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 176;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 304;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 432;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 5;
        }
        else if(i == 10)    //selesai
        {
            /*
            FORMASI
            01000000010
            00020002000
            00000100000
            00020002000
            01000000010
            */
            a = new SmallEnemy();
            a.x = 112;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 878;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 240;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 496;
            a.y = 814;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 368;
            a.y = 750;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 240;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new MediumEnemy();
            a.x = 496;
            a.y = 686;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 112;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            a = new SmallEnemy();
            a.x = 624;
            a.y = 622;
            a.width = 64;
            a.height = 64;
            enemies.add(a);

            scoreMultiplier = 5.5;
        }
        else if(i == 11)    //selesai
        {
            a = new Boss();
            a.x = 400-100;
            a.y = 750;
            a.height = 200;
            a.width = 200;
            enemies.add(a);
            bossState = true;

            scoreMultiplier = 10;
        }
        else                //selesai
        {
            getEnemyBatch(0);
        }
        lastSpawnTime = TimeUtils.nanoTime();
    }


    //============================================================FUNGSI BAWAAN YG TDK DIPAKE====================================================================================

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void pause()
    {

    }

    @Override
    public void resume()
    {

    }

    @Override
    public void hide()
    {

    }

    @Override
    public void dispose()
    {
        playerImg.dispose();
        smallEnemyImg.dispose();
        mediumEnemyImg.dispose();
        bossImg.dispose();
        laserImg.dispose();
        asteroidImg.dispose();
        font.dispose();
    }
}
