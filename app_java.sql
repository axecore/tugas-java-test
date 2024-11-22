-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 22, 2024 at 05:48 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `app_java`
--

-- --------------------------------------------------------

--
-- Table structure for table `jadwal`
--

CREATE TABLE `jadwal` (
  `id` int(11) NOT NULL,
  `user_id` varchar(250) DEFAULT NULL,
  `tanggal` varchar(250) DEFAULT NULL,
  `hari` varchar(250) DEFAULT NULL,
  `jam` varchar(250) DEFAULT NULL,
  `nama_pemohon` varchar(250) DEFAULT NULL,
  `nama_advokat` varchar(250) DEFAULT NULL,
  `asisten_advokat` varchar(250) DEFAULT NULL,
  `layanan` varchar(250) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `jadwal`
--

INSERT INTO `jadwal` (`id`, `user_id`, `tanggal`, `hari`, `jam`, `nama_pemohon`, `nama_advokat`, `asisten_advokat`, `layanan`) VALUES
(4, '1', '2024-11-23', 'SENIN', '14:00', 'ABDUL', 'BUBU', 'BABA', 'SIDANG');

-- --------------------------------------------------------

--
-- Table structure for table `kasus`
--

CREATE TABLE `kasus` (
  `id` int(11) NOT NULL,
  `no_registrasi` varchar(250) DEFAULT NULL,
  `no_identitas` varchar(250) DEFAULT NULL,
  `user_id` varchar(250) DEFAULT NULL,
  `nama` varchar(250) DEFAULT NULL,
  `jenis_kasus` varchar(250) DEFAULT NULL,
  `layanan` varchar(250) DEFAULT NULL,
  `kronologis_kasis` varchar(250) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `kasus`
--

INSERT INTO `kasus` (`id`, `no_registrasi`, `no_identitas`, `user_id`, `nama`, `jenis_kasus`, `layanan`, `kronologis_kasis`) VALUES
(9, 'REG-1234', '123-123-123', '1', 'asdasd', 'asdasdasd', '2024-11-22', 'adasdasd');

-- --------------------------------------------------------

--
-- Table structure for table `pemohon`
--

CREATE TABLE `pemohon` (
  `id` int(11) NOT NULL,
  `no_identintas` varchar(250) DEFAULT NULL,
  `nama` varchar(250) DEFAULT NULL,
  `jk` varchar(250) DEFAULT NULL,
  `alamat` varchar(250) DEFAULT NULL,
  `tempat_lahir` varchar(250) DEFAULT NULL,
  `tgl_lahir` varchar(250) DEFAULT NULL,
  `pekerjaan` varchar(250) DEFAULT NULL,
  `agama` varchar(250) DEFAULT NULL,
  `kewarganegaraan` varchar(250) DEFAULT NULL,
  `status` varchar(250) DEFAULT NULL,
  `no_telpon` varchar(250) DEFAULT NULL,
  `email` varchar(250) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `pemohon`
--

INSERT INTO `pemohon` (`id`, `no_identintas`, `nama`, `jk`, `alamat`, `tempat_lahir`, `tgl_lahir`, `pekerjaan`, `agama`, `kewarganegaraan`, `status`, `no_telpon`, `email`) VALUES
(2, '123-123-123', 'Liwar', 'Laki-Laki', 'jln', 'bjm', '2024-11-22', 'swasta', 'Islam', 'indo', 'Kawin', '08', 'asddddd@asdasdsad.com'),
(3, '234-234', 'pepek', 'Perempuan', 'jllll', 'bjb', '2024-11-22', 'gg', 'Islam', 'dd', 'Kawin', '2323', 'aldi@gmail.com');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(250) NOT NULL,
  `password` varchar(250) NOT NULL,
  `level` varchar(250) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `level`) VALUES
(1, 'aldi', 'popo', 'Admin');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `jadwal`
--
ALTER TABLE `jadwal`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `kasus`
--
ALTER TABLE `kasus`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `pemohon`
--
ALTER TABLE `pemohon`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `jadwal`
--
ALTER TABLE `jadwal`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `kasus`
--
ALTER TABLE `kasus`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `pemohon`
--
ALTER TABLE `pemohon`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
