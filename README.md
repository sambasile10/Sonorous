Java crypto library with full AES/RSA utility, all ciphers are streamed to allow concurrent development.
Built in objects for full file encryption/decryption. Work in Progress.

Usage: Sonorous.initialize(Module module)

=== Modules ===

Base - 100% working

Crypto - 100% working (small bugs may exist)

I/O - Mostly complete, additional fixes needed

==============

Basics:

Sonorous.getCipherFactory() returns a CipherFactory that can build AESCipherStream or RSACipherStream

Sonorous.getCipherUtil() returns a CipherUtil for hashing, key generation, key r/w to files, etc

Sonorous.getIOManager() returns a IOManager where an IOTask can be pushed and executed using startTask(IOTask task)