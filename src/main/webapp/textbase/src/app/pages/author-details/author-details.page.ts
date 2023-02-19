import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { AuthorDto } from 'src/app/dto/AuthorDto';
import { RestApiClientService } from 'src/app/rest-api-client.service';

@Component({
  selector: 'app-author-details',
  templateUrl: './author-details.page.html',
  styleUrls: ['./author-details.page.scss'],
})
export class AuthorDetailsPage implements OnInit {

  authorStrId: string;
  author: AuthorDto;
  
  constructor(private apiClient: RestApiClientService, private titleService: Title,
    private activatedRoute: ActivatedRoute) {
    
  }

  
  async ngOnInit() {
    this.authorStrId = this.activatedRoute.snapshot.paramMap.get('strid');
    this.author = await this.apiClient.getAuthor(this.authorStrId);
    this.author.image_href = this.author.image_href ? this.author.image_href : this.genericAvatar(this.author.strId);
    this.titleService.setTitle('TEXT ❖ BASE ' + this.author.firstName + " " + this.author.lastName);
  }
  
  genericAvatar(str: string) {
    const set = "initials";
    const res = `https://avatars.dicebear.com/api/initials/${str}.svg`;
    
    return res;
  }

}
