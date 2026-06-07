# Subway Truck

Subway Truck adalah game sederhana berbasis Java Swing. Game ini memakai mekanisme 3 lane seperti endless runner: player berada di bawah layar dan harus menghindari obstacle merah yang turun dari atas.

Game ini tidak memakai database dan tidak membutuhkan library eksternal. Semua data leaderboard hanya disimpan sementara di memori.

## Cara Menjalankan

Compile:

```powershell
javac SubwaySurferGame.java
```

Run:

```powershell
java SubwaySurferGame
```

## Fitur Utama

- Home screen dengan tombol `PLAY` dan `EXIT`
- 3 lane: kiri, tengah, kanan
- Player berpindah lane dengan keyboard
- Obstacle turun dari atas layar
- Collision detection antara player dan obstacle
- Game Over screen
- Input nama setelah game over
- Leaderboard top 5 sementara
- Restart game dengan tombol `R`
- Kembali ke Home dengan tombol `H`

## Alur Game

1. Aplikasi dibuka di halaman Home.
2. Player menekan tombol `PLAY`.
3. Game mulai berjalan dan score bertambah setiap tick.
4. Obstacle muncul secara acak dan bergerak turun.
5. Jika obstacle menabrak player, game berhenti.
6. Player memasukkan nama.
7. Score disimpan ke leaderboard sementara.
8. Player bisa restart atau kembali ke Home.

## Kontrol

| Tombol | Fungsi |
| --- | --- |
| Left Arrow | Pindah ke lane kiri |
| Right Arrow | Pindah ke lane kanan |
| Enter | Submit nama di input field |
| Submit | Simpan score |
| R | Restart saat Game Over |
| H | Kembali ke Home saat Game Over |

## Mekanisme Lane

Game memakai 3 lane:

- `0` = kiri
- `1` = tengah
- `2` = kanan

Player mulai dari lane tengah:

```java
int playerLane = 1;
```

Saat tombol kiri ditekan, nilai lane dikurangi. Saat tombol kanan ditekan, nilai lane ditambah. Nilai lane dibatasi agar tidak keluar dari `0` sampai `2`.

## Obstacle

Obstacle disimpan dalam:

```java
ArrayList<int[]> obstacles = new ArrayList<>();
```

Setiap obstacle memakai format:

```java
{laneIndex, yPosition}
```

Contoh:

```java
new int[] {1, -OBS_H}
```

Artinya obstacle muncul di lane tengah dan mulai dari atas layar.

## Game Loop

Game loop memakai `javax.swing.Timer`:

```java
Timer timer = new Timer(FPS, e -> update());
```

Setiap update:

- Score bertambah
- Tick bertambah
- Obstacle baru bisa muncul
- Obstacle bergerak turun
- Collision dicek
- Panel digambar ulang dengan `repaint()`

## Collision Detection

Collision terjadi jika:

- Lane obstacle sama dengan lane player
- Posisi Y obstacle menyentuh area Y player

Logika sederhananya:

```java
boolean sameLane = obs[0] == playerLane;
boolean sameY = obs[1] + OBS_H >= playerY && obs[1] <= playerY + PLAYER_H;
```

Jika `sameLane` dan `sameY` bernilai `true`, maka game over.

## Leaderboard

Leaderboard disimpan di memori:

```java
static ArrayList<ScoreEntry> leaderboard = new ArrayList<>();
```

Saat score baru masuk:

1. Data nama dan score ditambahkan.
2. Leaderboard diurutkan dari score terbesar.
3. Data dibatasi hanya top 5.

Karena tidak memakai database, leaderboard akan kosong lagi setelah program ditutup.

## Struktur Kode

Semua kode berada dalam satu file:

```text
SubwaySurferGame.java
```

Class utama:

- `SubwaySurferGame`: membuat window, CardLayout, dan mengatur pindah screen.
- `HomePanel`: tampilan awal game.
- `GamePanel`: gameplay, timer, keyboard input, rendering, dan game over.
- `ScoreEntry`: model sederhana untuk nama dan score.
- `NameFilter`: membatasi input nama maksimal 15 karakter.

## Catatan

File `.class` adalah hasil compile dari Java. File utama yang diedit adalah `SubwaySurferGame.java`.
