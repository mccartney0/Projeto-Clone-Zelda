package com.traduvertgames.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.traduvertgames.entities.Entity;
import com.traduvertgames.entities.Enemy;
import com.traduvertgames.entities.BulletShoot;
import com.traduvertgames.graficos.Spritesheet;
import com.traduvertgames.main.Game;
import com.traduvertgames.world.Camera;
import com.traduvertgames.world.World;

public class Player extends Entity {

	public boolean right, up, left, down;
	public int right_dir = 0, left_dir = 1, up_dir = 2, down_dir = 3;
	public int dir = right_dir;
	public double speed = 1.5;
	public static double life = 100, maxLife = 100;
	public static double mana = 0, maxMana = 500;
	public static double weapon = 0, maxWeapon = 250;
	public boolean damage = false;
//Animando o dano
	private int damageFrames = 0;
	int manaFrames = 0;
	boolean manaContinue = false;
	int manaSeconds = 0;
//	private boolean hasGun = false;

	public boolean shoot = false, mouseShoot = false;
	public int mx, my;

	public boolean jump = false;
	public boolean isJumping = false;

	public static int z = 0;

	public int jumpFrames = 50, jumpCur = 0;

	public boolean jumpUp = false, jumpDown = false;
	public int jumpSpd = 2;

	private int shootPerSecond = 1, time = 2;
	private int frames = 0, maxFrames = 7, index = 0, maxIndex = 3;
	private boolean moved = false;
	private BufferedImage[] rightPlayer;
	private BufferedImage[] leftPlayer;
	private BufferedImage[] upPlayer;
	private BufferedImage[] downPlayer;

	private BufferedImage playerDamage;
	private BufferedImage gunRight;
	private BufferedImage gunLeft;

	public Player(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);

		rightPlayer = new BufferedImage[4];
		leftPlayer = new BufferedImage[4];
		upPlayer = new BufferedImage[4];
		downPlayer = new BufferedImage[4];
		playerDamage = Game.spritesheet.getSprite(0, 16, 16, 16);
		gunRight = Game.spritesheet.getSprite(16, 16, 16, 16);
		gunLeft = Game.spritesheet.getSprite(0, 32, 16, 16);

		for (int i = 0; i < 4; i++) {
			rightPlayer[i] = Game.spritesheet.getSprite(32 + (i * 16), 0, 16, 16);
		}
		for (int i = 0; i < 4; i++) {
			leftPlayer[i] = Game.spritesheet.getSprite(32 + (i * 16), 16, 16, 16);
		}
		upPlayer[0] = Game.spritesheet.getSprite(32, 32, 16, 16);
		upPlayer[1] = Game.spritesheet.getSprite(32 + 16, 32, 16, 16);
		upPlayer[2] = Game.spritesheet.getSprite(32, 32, 16, 16);
		upPlayer[3] = Game.spritesheet.getSprite(32 + 16, 32, 16, 16);

		downPlayer[0] = Game.spritesheet.getSprite(64, 32, 16, 16);
		downPlayer[1] = Game.spritesheet.getSprite(64 + 16, 32, 16, 16);
		downPlayer[2] = Game.spritesheet.getSprite(64, 32, 16, 16);
		downPlayer[3] = Game.spritesheet.getSprite(64 + 16, 32, 16, 16);
	}

	public void update() {
//Fake jump 2D
		if (jump) {
			if (isJumping == false) {
				jump = false;
				isJumping = true;
				jumpUp = true;
			}
		}

		if (isJumping == true) {

			if (jumpUp) {
				jumpCur += jumpSpd;
			} else if (jumpDown) {
				jumpCur -= jumpSpd;
				if (jumpCur <= 0) {
					isJumping = false;
					jumpDown = false;
					jumpUp = false;
				}
			}
			z = jumpCur;
			if (jumpCur >= jumpFrames) {
				jumpUp = false;
				jumpDown = true;

			}
		}
// Fim Fake jump 2D 
		moved = false;
		if (right && World.isFree((int) (x + speed), this.getY(), z)) {
			moved = true;
			dir = right_dir;
			// Mover a c�mera - Colocar a camera para se mover com o jogador EX:
			// Camera.x+=speed;
			x += speed;
		} else if (left && World.isFree((int) (x - speed), this.getY(), z)) {
			moved = true;
			dir = left_dir;
			x -= speed;
		}
		if (up && World.isFree(this.getX(), (int) (y - speed), z)) {
			moved = true;
			dir = up_dir;
			y -= speed;
		} else if (down && World.isFree(this.getX(), (int) (y + speed), z)) {

			moved = true;
			dir = down_dir;
			y += speed;
		}
		if (moved) {
			frames++;
			if (frames == maxFrames) {
				frames = 0;
				index++;
				if (index > maxIndex) {
					index = 0;
				}
			}

			if (damage) {
				this.damageFrames++;
				if (this.damageFrames == 8) {// 8 milsegundo para a tile de dano ficar no personagem
					this.damageFrames = 0;
					damage = false;
				}
			}

			// Adicionando a c�mera com o Jogador sempre no meio da Tela
			// Renderizando o mapa com m�todo Clamp da Camera
//			Camera.x = Camera.clamp(this.getX() - (Game.WIDTH / 2), 0, World.WIDTH * 16 - Game.WIDTH);
//			Camera.y = Camera.clamp(this.getY() - (Game.HEIGHT / 2), 0, World.WIDTH * 16 - Game.HEIGHT);
		}
		this.checkCollisionLifePack();

		this.checkCollisionAmmo();

		this.checkCollisionGun();

		// Recuperando mana continuamente
		if (manaContinue == true) {
			this.manaFrames++;

			if (this.manaFrames == 80 && Player.mana < maxMana) {// 8 milsegundo para a tile de dano ficar no personagem
				mana += 8;
				manaSeconds++;
				this.manaFrames = 0;
			}
			if (manaSeconds == 5) {
				manaContinue = false;
				manaSeconds = 0;
			}

		}

		if (life <= 0) {
			// Game Over
			life = 0;
			weapon = 0;
			Game.gameState = "GAMEOVER";
		}
//Shoot com teclado
		if (shoot) {
			shoot = false;
			if (weapon > 0 && mana > 0) {
				// Criar bala e atirar
				weapon -= 0.2;
				mana--;
				shoot = false;
				int da = 0;
				int pd = 0;
				int pz = 8;
				if (dir == right_dir) {
					pd = 1;// x
					da = 3;

				} else {
					pd = -1;// y
					da = -3;
				}

				BulletShoot bullet = new BulletShoot(this.getX() + pd, this.getY() + pz, 3, 3, null, da, 0);
				Game.bullets.add(bullet);
			}
		}

		if (mouseShoot) {

			shootPerSecond++; // 2 tiros por segundo
			if (shootPerSecond == time) {
				shootPerSecond = 0;
				mouseShoot = false;
			}

			// Poder atirar
			if (weapon > 0 && mana > 0) {
				weapon -= 0.3;
				mana--;
				// Criar bala e atirar!
				int px = 0, py = 8;
				double angle = 0;
				if (dir == right_dir) {
					px = 8;
					angle = Math.atan2(my - (this.getY() + py - Camera.y), mx - (this.getX() + px - Camera.x));
				} else {
					px = 8;
					angle = Math.atan2(my - (this.getY() + py - Camera.y), mx - (this.getX() + px - Camera.x));
				}

				double dx = Math.cos(angle);
				double dy = Math.sin(angle);

				BulletShoot bullet = new BulletShoot(this.getX() + px, this.getY() + py, 3, 3, null, dx, dy);
				Game.bullets.add(bullet);
			}

		}
		updateCamera();
	}

// Adicionando a c�mera com o Jogador sempre no meio da Tela
// Renderizando o mapa com m�todo Clamp da Camera
	public void updateCamera() {
		Camera.x = Camera.clamp(this.getX() - (Game.WIDTH / 2), 0, World.WIDTH * 16 - Game.WIDTH);
		Camera.y = Camera.clamp(this.getY() - (Game.HEIGHT / 2), 0, World.HEIGHT * 16 - Game.HEIGHT);
	}

	// Método para fazer o Player não passar por cima do inimigo, a continuação
	// dele está na classe Enemys no follow path.
	public boolean isColiddingEnemys(int xnext, int ynext) {
		Rectangle player = new Rectangle(xnext + this.maskx + 2, ynext + this.masky + 2, this.mwidth - 4,
				this.mheight - 4);
		for (int i = 0; i < Game.enemies.size(); i++) {
			Enemy e = Game.enemies.get(i);
			Rectangle enemyCurrent = new Rectangle(e.getX() + e.maskx, e.getY() + e.masky, e.mwidth, e.mheight);
			if (enemyCurrent.intersects(player)) {
				return true;
			}
		}
		return false;
	}

	public void checkCollisionGun() {
		for (int i = 0; i < Game.entities.size(); i++) {
			Entity atual = Game.entities.get(i);
			if (atual instanceof Weapon) {
				if (Entity.isColliding(this, atual)) {
					// Pegou a arma
					if (weapon < 0) {
						weapon = 0;
					}
					if (weapon == 0) {
						weapon = maxWeapon;
					}
					weapon += 30;
					if (weapon >= maxWeapon)
						weapon = maxWeapon;
					// remove a arma
					Game.entities.remove(atual);
				}
			}
		}
	}

	public void checkCollisionLifePack() {
		for (int i = 0; i < Game.entities.size(); i++) {
			Entity atual = Game.entities.get(i);
			if (atual instanceof LifePack) {
				if (Entity.isColliding(this, atual)) {
					life += 40;
					if (life >= maxLife)
						life = maxLife;
					Game.entities.remove(atual);
				}
			}
		}
	}

	public void checkCollisionAmmo() {
		for (int i = 0; i < Game.entities.size(); i++) {
			Entity atual = Game.entities.get(i);
			if (atual instanceof Bullet) {
				if (Entity.isColliding(this, atual)) {
					manaContinue = true;
					mana += 50;
					if (mana >= maxMana) {
						mana = maxMana;
					}
					Game.entities.remove(atual);
				}
			}
		}
	}

	public void render(Graphics g) {
		if (!damage) {
			if (dir == right_dir) {
				g.drawImage(rightPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
				if (weapon >= 1) {
					// Desenhar arma para direita
					g.drawImage(Entity.GUN_RIGHT, this.getX() + 6 - Camera.x, this.getY() - Camera.y - z, null);
				}
			} else if (dir == left_dir) {
				g.drawImage(leftPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
				if (weapon >= 1) {
					// Desenhar arma para esquerda
					g.drawImage(Entity.GUN_LEFT, this.getX() - 6 - Camera.x, this.getY() - Camera.y - z, null);
				}
			}
			if (dir == up_dir) {
				g.drawImage(upPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
			}
			if (dir == down_dir) {
				g.drawImage(downPlayer[index], this.getX() - Camera.x, this.getY() - Camera.y - z, null);
			}
		} else {
			g.drawImage(playerDamage, this.getX() - Camera.x, this.getY() - Camera.y - z, null);
			if (weapon >= 1) {
				if (dir == left_dir) {
					g.drawImage(gunRight, this.getX() - 6 - Camera.x, this.getY() - Camera.y - z, null);
				} else {
					g.drawImage(gunLeft, this.getX() + 6 - Camera.x, this.getY() - Camera.y - z, null);
				}
			}
		}
		if (isJumping) {
			g.setColor(Color.black);
			g.fillOval(this.getX() - Camera.x + 8, this.getY() - Camera.y + 16, 6, 6);
		}
	}
}
