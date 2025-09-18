import React, { useState, useEffect } from "react";

export default function BookApp() {
  const [books, setBooks] = useState([]);
  const [title, setTitle] = useState("");
  const [author, setAuthor] = useState("");

  const fetchBooks = async () => {
    const res = await fetch("/books");
    setBooks(await res.json());
  };

  useEffect(() => { fetchBooks(); }, []);

  const addBook = async () => {
    await fetch("/books", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title, author })
    });
    setTitle(""); setAuthor("");
    fetchBooks();
  };

  const updateBook = async (id, newTitle) => {
    await fetch(`/books/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: newTitle, author: "Updated" })
    });
    fetchBooks();
  };

  const deleteBook = async (id) => {
    await fetch(`/books/${id}`, { method: "DELETE" });
    fetchBooks();
  };

  return (
    <div className="p-4">
      <h1 className="text-xl font-bold mb-4">Book Manager</h1>
      <div className="mb-4">
        <input value={title} onChange={e => setTitle(e.target.value)} placeholder="Title" />
        <input value={author} onChange={e => setAuthor(e.target.value)} placeholder="Author" />
        <button onClick={addBook}>Add</button>
      </div>
      <ul>
        {books.map(b => (
          <li key={b.id}>
            {b.title} - {b.author}
            <button onClick={() => updateBook(b.id, b.title + " (upd)")}>Update</button>
            <button onClick={() => deleteBook(b.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
