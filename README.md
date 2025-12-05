# EncryptX — File Encryption, Decryption, LAN Chat & File Sharing System

A Java-based desktop application that provides secure file encryption, decryption, offline LAN chatting, and file sharing between devices on the same network.
The project includes two encryption algorithms:

## AES Encryption (AES/CBC/PKCS5Padding)
## Manual XOR-Based Encryption Algorithm (Hardcoded)

Built using Java Swing, with an intuitive UI and drag-and-drop support.

## Features

### File Encryption & Decryption

AES 128-bit encryption (Java Crypto Library)

Manual XOR-based encryption (hardcoded)

Choose encryption method from a dropdown

Progress bar while encrypting/decrypting

Save and load AES keys

Supports any file type (PDF, MP3, JPG, EXE, ZIP, etc.)

### LAN Chat System (Offline Chatting)

Chat between two devices via same WiFi / LAN

Start server or connect as client

Send text messages in real time

Send files directly through chat

Completely offline (no cloud / no Internet)

### File Sharing

Share encrypted or normal files

Works through LAN chat window

No third-party dependency

Additional UI Features

Drag & Drop file selection

Modern Nimbus UI

Clean layout designed in NetBeans
---------------------------------------------------------

## Tech Stack

Java 8+

Java Swing

NetBeans IDE

AES/CBC/PKCS5Padding

Sockets (LAN Chat)
