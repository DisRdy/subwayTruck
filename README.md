# SubwaySurferGame

Game Java Swing sederhana dengan mekanisme lane seperti Subway Surfers. Player berada di bagian bawah layar dan bisa berpindah di antara 3 lane untuk menghindari obstacle yang turun dari atas.

Project ini tidak memakai database dan tidak memakai library eksternal. Leaderboard tetap ada, tetapi hanya disimpan sementara di memori selama aplikasi masih berjalan.

## Cara Menjalankan

Compile file utama:

```powershell
javac SubwaySurferGame.java
```

Jalankan game:

```powershell
java SubwaySurferGame
```

## Mekanisme Game

Game memakai 3 lane vertikal:

- Lane `0`: kiri
- Lane `1`: tengah
- Lane `2`: kanan

Player mulai dari lane tengah, yaitu `playerLane = 1`. Saat tombol panah kiri ditekan, lane player berkurang 1 sampai batas minimum 0. Saat tombol panah kanan ditekan, lane player bertambah 1 sampai batas maksimum 2.

Perpindahan lane terjadi langsung tanpa animasi. Ini membuat game sederhana dan mudah dipahami untuk versi awal.

## Game Loop

Game loop dibuat memakai `javax.swing.Timer` dengan delay 16 ms:

```java
timer = new Timer(16, event -> updateGame());
```

Delay 16 ms mendekati 60 FPS. Setiap tick, method `updateGame()` menjalankan beberapa hal:

- Menambah `tickCount`
- Menambah `score`
- Mengecek apakah waktunya spawn obstacle baru
- Menggerakkan semua obstacle ke bawah
- Mengecek collision antara player dan obstacle
- Memanggil `repaint()` agar layar digambar ulang

Kalau game sudah over, timer dihentikan dengan `timer.stop()` supaya game benar-benar pause.

## Obstacle

Obstacle disimpan dalam:

```java
ArrayList<int[]> obstacles = new ArrayList<>();
```

Setiap obstacle berbentuk array integer:

```java
{laneIndex, yPosition}
```

Contoh:

```java
new int[] {2, -50}
```

Artinya obstacle muncul di lane kanan dan mulai dari posisi atas layar.

Obstacle baru dibuat setiap 60 sampai 90 tick secara acak:

```java
private int randomSpawnInterval() {
    return 60 + random.nextInt(31);
}
```

Setiap tick, obstacle turun dengan kecepatan tetap:

```java
obstacle[1] += OBSTACLE_SPEED;
```

Obstacle yang sudah melewati bagian bawah panel akan dihapus dari list.

## Collision

Collision terjadi jika:

- Obstacle berada di lane yang sama dengan player
- Area Y obstacle sudah menyentuh area Y player

Logika utamanya ada di `checkCollisions()`:

```java
if (obstacleLane == playerLane && obstacleBottom >= playerY && obstacleY <= playerBottom) {
    handleGameOver();
}
```

Jika collision terjadi, game masuk ke mode game over.

## Game Over dan Input Nama

Saat game over:

- Timer dihentikan
- Teks `GAME OVER` ditampilkan
- Final score ditampilkan
- Input nama dan tombol `Submit` muncul

Nama player divalidasi sebelum disimpan:

- Spasi di awal dan akhir dihapus dengan `trim()`
- Nama kosong ditolak
- Panjang nama dibatasi maksimal 15 karakter

Pembatasan 15 karakter dibuat memakai `DocumentFilter` di class `NameLengthFilter`.

Setelah submit:

- Score masuk ke leaderboard memori
- Leaderboard diurutkan dari score terbesar
- Hanya top 5 yang ditampilkan
- Input nama dan tombol submit disembunyikan
- Pesan `Press R to Restart` muncul

## Leaderboard

Leaderboard digambar di kanan atas layar oleh method `drawLeaderboard(Graphics2D g2)`.

Data score lengkap disimpan sementara di:

```java
private final ArrayList<String[]> allScores = new ArrayList<>();
```

Data top 5 yang ditampilkan disimpan di:

```java
private List<String[]> leaderboard = new ArrayList<>();
```

Saat score baru disubmit, method `refreshLeaderboard()` akan:

- Menyalin semua score
- Mengurutkan score dari terbesar ke terkecil
- Mengambil maksimal 5 data teratas
- Memperbarui tampilan leaderboard

Karena tidak memakai database, leaderboard akan reset saat aplikasi ditutup.

## Struktur Kode

File utama adalah `SubwaySurferGame.java`.

### `SubwaySurferGame extends JFrame`

Class utama yang membuat window game. Di constructor, class ini:

- Mengatur judul window
- Mengatur close operation
- Membuat `GamePanel`
- Memasang panel ke frame
- Mengatur ukuran window dari ukuran panel
- Meminta fokus keyboard ke game panel

### `GamePanel extends JPanel implements KeyListener`

Class ini berisi hampir semua logic game, termasuk:

- State player
- State obstacle
- Score
- Leaderboard
- Timer game loop
- Gambar UI
- Input keyboard
- Input nama saat game over

### Method Penting

`updateGame()`

Menjalankan logic utama setiap tick.

`spawnObstacle()`

Membuat obstacle baru di lane acak.

`moveObstacles()`

Menggerakkan obstacle ke bawah dan menghapus obstacle yang keluar layar.

`checkCollisions()`

Mengecek apakah obstacle bertabrakan dengan player.

`handleGameOver()`

Mengubah state menjadi game over, menghentikan timer, dan menampilkan input nama.

`submitScore()`

Memvalidasi nama, menyimpan score ke memori, dan memperbarui leaderboard.

`restartGame()`

Mengembalikan game ke kondisi awal:

- Player kembali ke lane tengah
- Obstacle dibersihkan
- Score kembali 0
- Timer jalan lagi

`paintComponent(Graphics graphics)`

Method utama untuk menggambar tampilan game. Method ini memanggil:

- `drawLanes()`
- `drawPlayer()`
- `drawObstacles()`
- `drawScore()`
- `drawLeaderboard()`
- `drawGameOver()` jika game over

## Kontrol

- Panah kiri: pindah ke lane kiri
- Panah kanan: pindah ke lane kanan
- Enter di input nama: submit score
- Tombol Submit: submit score
- R: restart setelah score disubmit

## Catatan

Game ini sengaja dibuat sederhana agar mudah dipelajari. Beberapa fitur yang bisa ditambahkan nanti:

- Animasi perpindahan lane
- Kecepatan obstacle meningkat seiring score
- Koin atau power-up
- Sprite gambar untuk player dan obstacle
- Leaderboard permanen dengan file atau database
