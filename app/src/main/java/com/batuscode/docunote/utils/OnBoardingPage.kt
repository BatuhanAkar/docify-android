package com.batuscode.docunote.utils

import androidx.annotation.DrawableRes
import com.batuscode.docunote.R


sealed class OnBoardingPage(
    @DrawableRes
    val image : Int ,
    val title : String ,
    val description : String ,
) {

    object First : OnBoardingPage(
        image = R.drawable.ic_launcher_foreground ,
        title = "Welcome to Docify" ,
        description = "Let’s get started with a quick tour. We’ll show you how to get the best experience from Docify and share what we’re all about."
    )

    object Second : OnBoardingPage(
        image = R.drawable.baseline_question_mark_24 ,
        title = "About Docify AI" ,
        description = "Docify AI is designed to help you interact with PDF documents in two powerful ways: Summarization and Chat-based Q&A.\n" +
                "\n" +
                "Summarization: Don’t have time to read a long document? Let Docify AI create a concise summary so you can grasp the key points in seconds and save valuable time.\n" +
                "Chat with Your PDF: Start a conversation with your document! Ask questions and get instant answers based on the content of your PDF—perfect for quickly finding the information you need.\n" +
                "Effortless document understanding starts here."
    )

    object Third : OnBoardingPage(
        image = R.drawable.ic_launcher_foreground ,
        title = "Designed for Smart, Efficient Use" ,
        description = "Designed for Smart, Efficient Use\n" +
                "At Docify AI, we believe in speed, clarity, and purpose.\n" +
                "\n" +
                "That’s why we’ve optimized the experience to work best with focused, high-impact documents.\n" +
                "\uD83D\uDD39 Upload PDFs up to 50MB – the sweet spot for fast, accurate summaries and chat responses.\n" +
                "\uD83D\uDD39 No distractions. No bloated files. Just pure insight, delivered instantly.\n" +
                "\n" +
                "It’s not a limit – it’s a commitment to performance.\n" +
                "Let’s keep it sharp. Let’s keep it smart."
    )

    object Fourth : OnBoardingPage(
        image = R.drawable.workspaces ,
        title = "Your Personal PDF Workspace — And It’s Free" ,
        description = "Need to open, read, edit, or save your PDF files?\n" +
                "With Docify Workspace, you get a clean, intuitive space to manage your documents with ease.\n" +
                "\uD83D\uDCC2 Open any PDF in seconds\n" +
                "✏\uFE0F Highlight, annotate, and make edits effortlessly\n" +
                "\uD83D\uDCBE Save your changes instantly\n" +
                "\n" +
                "Whether you're preparing notes or just reviewing a document, Docify keeps things simple — and powerful.\n" +
                "\n" +
                "Best of all? It’s 100% free.\n" +
                "No subscriptions, no paywalls — just everything you need, in one place."
    )
}