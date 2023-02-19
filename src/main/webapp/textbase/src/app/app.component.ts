import { Component } from '@angular/core';
@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public appPages = [
    // { title: 'Featured', url: '/featured', icon: 'archive' },
    { title: 'Authors', url: '/authors', icon: 'mail' },
    // { title: 'Languages', url: '/languages', icon: 'paper-plane' },
    // { title: 'Collections', url: '/collections', icon: 'heart' },
    // { title: 'Trash', url: '/folder/Trash', icon: 'trash' },
    // { title: 'Spam', url: '/folder/Spam', icon: 'warning' },
  ];
  public labels = ['Family', 'Friends', 'Notes', 'Work', 'Travel', 'Reminders'];
  constructor() {}
}
